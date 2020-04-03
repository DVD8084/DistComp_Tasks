package lab

import (
	"fmt"
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

const amount = 4
var score = 0
var duccMutex = sync.Mutex{}
var duccs = make([]*Ducc, amount)
var duccPresent = make([]bool, amount)
var duccChannel = make(chan int, amount)

// Run implements the main program loop. It returns when the platform signals to stop.
func Run(p Platform, r Renderer) {
	InitDucc(r)
	consolas := imgui.CurrentIO().Fonts().AddFontFromFileTTF("res/consola.ttf", 24)
	consolasHuge := imgui.CurrentIO().Fonts().AddFontFromFileTTF("res/consola.ttf", 36)
	r.CreateFontsTexture()

	clearColor := [3]float32{0.25, 0.75, 0.25}

	showStart := true
	started := false

	duccMutex.Lock()
	for i := 0; i < amount; i++ {
		duccChannel <- i
	}
	duccMutex.Unlock()

	for !p.ShouldStop() {
		p.ProcessEvents()

		// Signal start of a new frame
		p.NewFrame()
		imgui.NewFrame()

		duccMutex.Lock()

		for i := 0; i < amount; i++ {
			if duccPresent[i] {
				duccs[i].Display(&score)
			}
		}

		imgui.PushFont(consolas)
		imgui.SetNextWindowPos(imgui.Vec2{X: 5, Y: 5})
		imgui.BeginV("score", &showStart, imgui.WindowFlagsNoMove|imgui.WindowFlagsNoTitleBar|imgui.WindowFlagsAlwaysAutoResize)
		imgui.Text(fmt.Sprintf("Score: %v", score))
		imgui.PopFont()
		imgui.End()

		duccMutex.Unlock()

		if showStart {
			imgui.PushFont(consolasHuge)
			imgui.BeginV("start", &showStart, imgui.WindowFlagsNoTitleBar|imgui.WindowFlagsAlwaysAutoResize)
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
	for ;; {
		oldDucc := <-duccChannel
		duccMutex.Lock()
		duccPresent[oldDucc] = false
		duccMutex.Unlock()
		_ = wait(200, 500)
		duccMutex.Lock()
		newDucc := 0
		for ; duccPresent[newDucc]; {
			newDucc++
		}
		duccs[newDucc] = NewDucc()
		duccPresent[newDucc] = true
		go RunDucc(duccs[newDucc], newDucc, duccChannel)
		duccMutex.Unlock()
	}
}



