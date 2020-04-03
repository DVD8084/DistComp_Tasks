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

const length = 15
const volume = 10
const bees = 5

var honeyLevel = 0
var honeySemaphore = make(chan bool)
var honeyMutex = sync.Mutex{}
var beePositions = [bees]int{}
var beeChannel = make(chan bool, bees)
var beeMutex = sync.Mutex{}
var bearOut = false

// Run implements the main program loop. It returns when the platform signals to stop.
func Run(p Platform, r Renderer) {
	//consolas := imgui.CurrentIO().Fonts().AddFontFromFileTTF("res/consola.ttf", 24)
	consolasHuge := imgui.CurrentIO().Fonts().AddFontFromFileTTF("res/consola.ttf", 36)
	r.CreateFontsTexture()

	bear := r.CreateImageTexture("res/Lazy_Panda.png")
	bee := r.CreateImageTexture("res/Bee.gif")
	beeRight := r.CreateImageTexture("res/Bee_Right.gif")
	door := r.CreateImageTexture("res/Dark_Oak_Door.png")
	honey := r.CreateImageTexture("res/Honey.png")
	none := r.CreateImageTexture("res/none.png")
	pot := r.CreateImageTexture("res/Flower_Pot.png")
	tree := r.CreateImageTexture("res/Oak_Tree.png")
	treeNest := r.CreateImageTexture("res/Oak_Tree_with_Bee_nest.png")

	clearColor := [3]float32{0.25, 0.75, 0.25}

	for i := 0; i < bees; i++ {
		beePositions[i] = -1
	}

	showForest := true
	showStart := true
	started := false

	for !p.ShouldStop() {
		p.ProcessEvents()

		// Signal start of a new frame
		p.NewFrame()
		imgui.NewFrame()

		beeMutex.Lock()

		imgui.BeginV("Hundred Acre Wood", &showForest, imgui.WindowFlagsNoTitleBar|imgui.WindowFlagsAlwaysAutoResize)
		imgui.Image(treeNest, imgui.Vec2{X: 50, Y: 50})
		imgui.SameLine()
		for i := 0; i < length; i++ {
			beeFound := false
			for j := 0; j < bees; j++ {
				if beePositions[j] == i {
					beeFound = true
					break
				}
			}
			if beeFound {
				imgui.Image(beeRight, imgui.Vec2{X: 50, Y: 50})
			} else {
				imgui.Image(none, imgui.Vec2{X: 50, Y: 50})
			}
			imgui.SameLine()
		}
		imgui.Image(pot, imgui.Vec2{X: 50, Y: 50})
		imgui.Image(tree, imgui.Vec2{X: 50, Y: 50})
		imgui.SameLine()
		for i := 0; i < length; i++ {
			beeFound := false
			for j := 0; j < bees; j++ {
				if beePositions[j] == length * 2 - i {
					beeFound = true
					break
				}
			}
			if beeFound {
				imgui.Image(bee, imgui.Vec2{X: 50, Y: 50})
			} else {
				imgui.Image(none, imgui.Vec2{X: 50, Y: 50})
			}
			imgui.SameLine()
		}
		if bearOut {
			imgui.Image(bear, imgui.Vec2{X: 50, Y: 50})
		} else {
			imgui.Image(door, imgui.Vec2{X: 50, Y: 50})
		}
		imgui.End()

		beeMutex.Unlock()

		hl := honeyLevel

		imgui.BeginV("Honey", &showForest, imgui.WindowFlagsNoTitleBar|imgui.WindowFlagsAlwaysAutoResize)
		imgui.Image(none, imgui.Vec2{X: 50, Y: float32(20 * (volume - hl))})
		imgui.Image(honey, imgui.Vec2{X: 50, Y: float32(20 * hl)})
		imgui.End()


		if showStart {
			imgui.PushFont(consolasHuge)
			imgui.BeginV("Start", &showStart, imgui.WindowFlagsNoTitleBar|imgui.WindowFlagsAlwaysAutoResize)
			if imgui.Button("Start") {
				showStart = false
			}
			imgui.End()
			imgui.PopFont()
		} else if !started {
			go Start()
			started = true
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
	go Bees()
	go Bear()
	for i := 0; i < bees; i++ {
		beeChannel <- true
		<-time.After(time.Millisecond * time.Duration(rand.Intn(400) + 300))
	}
}

func Bees() {
	beeID := 0
	for ; true; {
		<-beeChannel
		go sendBees(beeID)
		beeID += 1
		beeID %= bees
	}
}

func sendBees(i int) {
	for j := 0; j < length; j++ {
		moveBee(i, j)
		<-time.After(time.Millisecond * time.Duration(rand.Intn(400) + 100))
	}
	moveBee(i, length)
	<-time.After(time.Millisecond * time.Duration(rand.Intn(400) + 300))
	honeyMutex.Lock()
	honeyLevel += 1
	if honeyLevel == volume {
		honeySemaphore <- true
	}
	honeyMutex.Unlock()
	for j := 0; j < length; j++ {
		moveBee(i, length + j + 1)
		<-time.After(time.Millisecond * time.Duration(rand.Intn(400) + 100))
	}
	moveBee(i, -1)
	<-time.After(time.Millisecond * time.Duration(rand.Intn(400) + 500))
	beeChannel <- true
}

func moveBee(i int, pos int) {
	beeMutex.Lock()
	beePositions[i] = pos
	beeMutex.Unlock()
}

func Bear() {
	for ; true; {
		<-honeySemaphore
		bearOut = true
		honeyMutex.Lock()
		for ; honeyLevel > 0; {
			<-time.After(time.Millisecond * time.Duration(rand.Intn(400) + 300))
			honeyLevel -= 1
		}
		honeyMutex.Unlock()
		bearOut = false
	}
}
