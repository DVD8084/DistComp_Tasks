package lab

import (
	"math/rand"
	"sync"
	"time"

	"github.com/inkyblackness/imgui-go/v2"
)

// Platform covers mouse/keyboard/gamepad inputs, cursor shape, timing, windowing.
type Platform interface {
	// ShouldStop is regularly called as the abort condition for the program loop.
	ShouldStop() bool
	// ProcessEvents is called once per render loop to dispatch any pending events.
	ProcessEvents()
	// DisplaySize returns the dimension of the display.
	DisplaySize() [2]float32
	// FramebufferSize returns the dimension of the framebuffer.
	FramebufferSize() [2]float32
	// NewFrame marks the begin of a render pass. It must update the imgui IO state according to user input (mouse, keyboard, ...)
	NewFrame()
	// PostRender marks the completion of one render pass. Typically this causes the display buffer to be swapped.
	PostRender()
	// ClipboardText returns the current text of the clipboard, if available.
	ClipboardText() (string, error)
	// SetClipboardText sets the text as the current text of the clipboard.
	SetClipboardText(text string)
}

type clipboard struct {
	platform Platform
}

func (board clipboard) Text() (string, error) {
	return board.platform.ClipboardText()
}

func (board clipboard) SetText(text string) {
	board.platform.SetClipboardText(text)
}

// Renderer covers rendering imgui draw data.
type Renderer interface {
	// PreRender causes the display buffer to be prepared for new output.
	PreRender(clearColor [3]float32)
	// Render draws the provided imgui draw data.
	Render(displaySize [2]float32, framebufferSize [2]float32, drawData imgui.DrawData)

	CreateFontsTexture()
	CreateImageTexture(path string) imgui.TextureID
}

const lines = 9
const length = 10
const bees = 5

var beePos = [lines]int{}
var bearPos = lines / 2
var beeSearch = [lines]int{}

var beeMutex = sync.Mutex{}
var bearMutex = sync.Mutex{}

var bearFound = false
var bearPunished = false
var hiveChannel = make(chan bool, bees)

// Run implements the main program loop. It returns when the platform signals to stop.
func Run(p Platform, r Renderer) {
	consolas := imgui.CurrentIO().Fonts().AddFontFromFileTTF("res/consola.ttf", 24)
	consolasHuge := imgui.CurrentIO().Fonts().AddFontFromFileTTF("res/consola.ttf", 36)
	r.CreateFontsTexture()

	tree := r.CreateImageTexture("res/Oak_Tree.png")
	bee := r.CreateImageTexture("res/Bee.gif")
	beeAngry := r.CreateImageTexture("res/Bee_(angry).gif")
	bear := r.CreateImageTexture("res/Lazy_Panda.png")
	bearHurt := r.CreateImageTexture("res/Worried_Panda.png")

	clearColor := [3]float32{0.25, 0.75, 0.25}

	for i := 0; i <  lines; i++ {
		beePos[i] = -1
	}

	showForest := true
	showStart := true
	started := false

	for !p.ShouldStop() {
		p.ProcessEvents()

		// Signal start of a new frame
		p.NewFrame()
		imgui.NewFrame()

		imgui.PushFont(consolas)
		imgui.BeginV("Hundred Acre Wood", &showForest, imgui.WindowFlagsNoTitleBar|imgui.WindowFlagsAlwaysAutoResize)
		if showStart {
			imgui.Text("The Bees are waiting...")
		} else if bearFound {
				imgui.Text("The Bees have found Pooh!")
		} else {
			imgui.Text("The Bees are looking...")
		}
		beeMutex.Lock()
		bearMutex.Lock()
		for i := 0; i < length; i++ {
			for j := 0; j < lines; j++ {
				if beePos[j] == i {
					if bearPos == j && i == length-1 {
						imgui.Image(beeAngry, imgui.Vec2{X: 50, Y: 50})
					} else {
						imgui.Image(bee, imgui.Vec2{X: 50, Y: 50})
					}
				} else if bearPos == j && i == length-1 {
					if bearPunished {
						imgui.Image(bearHurt, imgui.Vec2{X: 50, Y: 50})
					} else {
						imgui.Image(bear, imgui.Vec2{X: 50, Y: 50})
					}
				} else {
					imgui.Image(tree, imgui.Vec2{X: 50, Y: 50})
				}
				if j < lines-1 {
					imgui.SameLine()
				}
			}
		}
		beeMutex.Unlock()
		bearMutex.Unlock()
		imgui.End()
		imgui.PopFont()

		if showStart {
			imgui.PushFont(consolasHuge)
			imgui.BeginV("Bee Leash", &showStart, imgui.WindowFlagsNoTitleBar|imgui.WindowFlagsAlwaysAutoResize)
			if imgui.Button("Unleash the Wrath of Bees!") {
				showStart = false
			}
			imgui.End()
			imgui.PopFont()
		} else if !started {
			go Start()
			started = true
		} else {

		}

		// Rendering
		imgui.Render() // This call only creates the draw data list. Actual rendering to framebuffer is done below.

		r.PreRender(clearColor)
		// A this point, the application could perform its own rendering...
		// app.RenderScene()

		r.Render(p.DisplaySize(), p.FramebufferSize(), imgui.RenderedDrawData())
		p.PostRender()

		// sleep to avoid 100% CPU usage
		<-time.After(time.Millisecond * 25)
	}
}

func Start() {
	for i := 0; i < lines; i++ {
		beeSearch[i] = 0
	}

	bearFound = false
	for i := 0; i < bees; i++ {
		hiveChannel <- false
	}

	go Bear()

	for ; !bearFound ; {
		bearFound = <-hiveChannel
		if bearFound {
			break
		}
		beeMutex.Lock()
		smallestBeeSearch := -1
		sendBeesAt := -1
		for i := 0; i < lines; i++ {
			if (beeSearch[i] < smallestBeeSearch || smallestBeeSearch == -1) && beeSearch[i] % 2 == 0 {
				smallestBeeSearch = beeSearch[i]
				sendBeesAt = i
			}
		}
		beeSearch[sendBeesAt]++
		beeMutex.Unlock()
		go Bees(sendBeesAt)
	}
}

func Bees(line int) {
	for i := 0; i < length; i++ {
		moveBees(line, 1)
		<-time.After(time.Millisecond * time.Duration(rand.Intn(400) + 100))
	}
	found := false
	if foundBear(line) {
		found = true
		<-time.After(time.Millisecond * 1000)
	}
	for i := 0; i < length; i++ {
		moveBees(line, -1)
		<-time.After(time.Millisecond * time.Duration(rand.Intn(400) + 100))
	}
	beeMutex.Lock()
	beeSearch[line]++
	beeMutex.Unlock()
	hiveChannel <- found
}

func moveBees(line int, direction int) {
	beeMutex.Lock()
	beePos[line] += direction
	if beePos[line] > length - 1 {
		beePos[line] = length - 1
	}
	if beePos[line] < -1 {
		beePos[line] = -1
	}
	beeMutex.Unlock()
}

func foundBear(line int) bool {
	bearMutex.Lock()
	bear := bearPos == line
	if bear {
		bearPunished = true
	}
	bearMutex.Unlock()
	return bear
}

func Bear() {
	for ; true ; {
		moveBear()
		<-time.After(time.Millisecond * time.Duration(rand.Intn(500)+750))
	}
}

func moveBear() {
	bearMutex.Lock()

	if !bearPunished {
		leftSideFree := bearPos > 0
		rightSideFree := bearPos < lines-1

		if leftSideFree && rightSideFree {
			bearPos += rand.Intn(3) - 1
		} else if leftSideFree {
			bearPos += rand.Intn(2) - 1
		} else if rightSideFree {
			bearPos += rand.Intn(2)
		}
	}

	bearMutex.Unlock()
}