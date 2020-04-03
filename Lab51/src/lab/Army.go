package lab

import (
	"fmt"
	"github.com/inkyblackness/imgui-go/v2"
	"math/rand"
	"sync"
)

var sheep      imgui.TextureID
var sheepLeft  imgui.TextureID
var sheepRight imgui.TextureID
var sheepDerp  imgui.TextureID
var font       imgui.Font

type Army struct {
	length   int
	soldiers []int
	mutex    sync.Mutex
}

func NewArmy(length int) (*Army, error) {
	if length < 50  {
		return nil, fmt.Errorf("should have at least 50 soldiers (got %v)", length)
	}
	army := &Army{
		length:   length,
		soldiers: make([]int, length),
		mutex: sync.Mutex{},
	}
	for i := 0; i < length; i++ {
		army.soldiers[i] = rand.Intn(11) / 10 * 10
	}
	return army, nil
}

func InitArmy(r Renderer) {
	sheep = r.CreateImageTexture("res/sheep.png")
	sheepLeft = r.CreateImageTexture("res/sheep_left.png")
	sheepRight = r.CreateImageTexture("res/sheep_right.png")
	sheepDerp = r.CreateImageTexture("res/sheep_derp.png")
	font = imgui.CurrentIO().Fonts().AddFontFromFileTTF("res/consola.ttf", 36)
}

func (army *Army) Display(size float32) {
	army.mutex.Lock()
	show := true
	imgui.BeginV("Army", &show, imgui.WindowFlagsNoTitleBar|imgui.WindowFlagsAlwaysAutoResize)
	for i := 0; i < army.length; i++ {
		switch army.soldiers[i] {
		case 0: imgui.Image(sheep, imgui.Vec2{X: size, Y: size})
		case -1: imgui.Image(sheepLeft, imgui.Vec2{X: size, Y: size})
		case 1: imgui.Image(sheepRight, imgui.Vec2{X: size, Y: size})
		case 10: imgui.Image(sheepDerp, imgui.Vec2{X: size, Y: size})
		}
		if i < army.length - 1 {
			imgui.SameLine()
		}
	}
	imgui.End()
	army.mutex.Unlock()
}

func (army *Army) Initialize() {
	army.mutex.Lock()
	for i := 0; i < army.length; i++ {
		army.soldiers[i] = rand.Intn(2) * 2 - 1
	}
	army.mutex.Unlock()
}

func (army *Army) Update() {
	for i := 0; i < army.length; i++ {
		army.mutex.Lock()
		if i + army.soldiers[i] == -1 || i + army.soldiers[i] == army.length {
		} else if army.soldiers[i + army.soldiers[i]] == -army.soldiers[i] {
			army.soldiers[i + army.soldiers[i]] = army.soldiers[i]
			army.soldiers[i] = -army.soldiers[i]
		}
		army.mutex.Unlock()
		_ = wait(10, 20)
	}
}

func (army *Army) InOrder() bool {
	for i := 1; i < army.length; i++ {
		if army.soldiers[i] != army.soldiers[0] {
			return false
		}
	}
	return true
}