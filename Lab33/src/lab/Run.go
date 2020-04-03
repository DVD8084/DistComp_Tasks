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

var graphicMutex = sync.Mutex{}
var villagerSemaphores = [3]chan bool{make(chan bool), make(chan bool), make(chan bool)}
var traderSemaphore = make(chan bool)
var hasStew = [3]bool{false, false, false}
var trades = [2]int{0, 0}

// Run implements the main program loop. It returns when the platform signals to stop.
func Run(p Platform, r Renderer) {
	//consolas := imgui.CurrentIO().Fonts().AddFontFromFileTTF("res/consola.ttf", 24)
	consolasHuge := imgui.CurrentIO().Fonts().AddFontFromFileTTF("res/consola.ttf", 36)
	r.CreateFontsTexture()

	nitwit := r.CreateImageTexture("res/Nitwit.png")
	trader := r.CreateImageTexture("res/Wandering_Trader.png")

	bowl := r.CreateImageTexture("res/Bowl.png")
	brown := r.CreateImageTexture("res/Brown_Mushroom.png")
	red := r.CreateImageTexture("res/Red_Mushroom.png")
	stew := r.CreateImageTexture("res/Mushroom_Stew.png")

	none := r.CreateImageTexture("res/none.png")

	ingredients := [4]imgui.TextureID{none, bowl, brown, red}

	clearColor := [3]float32{0.75, 0.5, 0.25}

	showStart := true
	started := false

	for !p.ShouldStop() {
		p.ProcessEvents()

		// Signal start of a new frame
		p.NewFrame()
		imgui.NewFrame()

		graphicMutex.Lock()
		imgui.BeginV("Villagers", &showStart, imgui.WindowFlagsNoTitleBar|imgui.WindowFlagsAlwaysAutoResize)
		for i := 0; i < 3; i++ {
			imgui.Image(nitwit, imgui.Vec2{X: 75, Y: 150})
			imgui.SameLine()
		}
		imgui.Image(trader, imgui.Vec2{X: 75, Y: 150})

		for i := 0; i < 3; i++ {
			imgui.Image(ingredients[i + 1], imgui.Vec2{X: 75, Y: 75})
			imgui.SameLine()
		}
		imgui.Image(ingredients[trades[0]], imgui.Vec2{X: 75, Y: 75})

		for i := 0; i < 3; i++ {
			if hasStew[i] {
				imgui.Image(stew, imgui.Vec2{X: 75, Y: 75})
			} else {
				imgui.Image(none, imgui.Vec2{X: 75, Y: 75})
			}
			imgui.SameLine()
		}
		imgui.Image(ingredients[trades[1]], imgui.Vec2{X: 75, Y: 75})
		imgui.End()
		graphicMutex.Unlock()

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
	go Villager(0)
	go Villager(1)
	go Villager(2)
	go Trader()
}

func Trader() {
	for ; true; {
		victim := rand.Intn(3)
		order := rand.Intn(2)
		graphicMutex.Lock()
		trades[0] = (victim + 1 + order) % 3 + 1
		graphicMutex.Unlock()
		<-time.After(time.Millisecond * time.Duration(rand.Intn(400) + 500))
		graphicMutex.Lock()
		trades[1] = (victim + 2 - order) % 3 + 1
		graphicMutex.Unlock()
		<-time.After(time.Millisecond * time.Duration(rand.Intn(400) + 500))
		villagerSemaphores[victim] <- true
		<-traderSemaphore
	}
}

func Villager(i int) {
	for ; true; {
		<-villagerSemaphores[i]
		graphicMutex.Lock()
		trades[0] = 0
		graphicMutex.Unlock()
		<-time.After(time.Millisecond * time.Duration(rand.Intn(200) + 50))
		graphicMutex.Lock()
		trades[1] = 0
		graphicMutex.Unlock()
		<-time.After(time.Millisecond * time.Duration(rand.Intn(200) + 50))
		graphicMutex.Lock()
		hasStew[i] = true
		graphicMutex.Unlock()
		<-time.After(time.Millisecond * time.Duration(rand.Intn(400) + 1250))
		graphicMutex.Lock()
		hasStew[i] = false
		graphicMutex.Unlock()
		traderSemaphore <- true
	}
}

