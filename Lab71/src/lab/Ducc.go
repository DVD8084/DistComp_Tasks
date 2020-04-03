package lab

import (
	"fmt"
	"github.com/inkyblackness/imgui-go/v2"
	"math/rand"
	"sync"
)

var duccPic     imgui.TextureID
var idCounter = staticCounter()

type Ducc struct {
	position []float32
	size     float32
	speed    []float32
	alive    bool
	mutex    sync.Mutex
	id       int
}

func InitDucc(r Renderer) {
	duccPic = r.CreateImageTexture("res/Chicken.png")
}

func NewDucc() *Ducc {
	ducc := &Ducc{
		position: make([]float32, 2),
		size:     rand.Float32() * 30 + 40,
		speed:    []float32{rand.Float32() * 5 + 1, rand.Float32() * 6 - 2},
		alive:    true,
		mutex:    sync.Mutex{},
		id:       idCounter(),
	}

	if rand.Intn(2) == 1 {
		ducc.position[0] = 1080
		ducc.speed[0] = -ducc.speed[0]
	} else {
		ducc.position[0] = 0
	}

	ducc.position[1] = rand.Float32() * 300 + 150

	return ducc
}

func (ducc *Ducc) Display(score *int) {
	ducc.mutex.Lock()
	show := true
	imgui.SetNextWindowPosV(imgui.Vec2{X: ducc.position[0], Y: ducc.position[1]}, 0, imgui.Vec2{X: 0.5, Y: 0.5})
	imgui.BeginV(fmt.Sprintf("ducc##%v", ducc.id), &show, imgui.WindowFlagsNoMove|imgui.WindowFlagsNoTitleBar|imgui.WindowFlagsAlwaysAutoResize)
	imgui.PushID(string(ducc.id))
	if imgui.ImageButton(duccPic, imgui.Vec2{X: ducc.size, Y: ducc.size}) {
		ducc.die()
		*score++
	}
	imgui.PopID()
	imgui.End()
	ducc.mutex.Unlock()
}

func (ducc *Ducc) move() {
	ducc.mutex.Lock()
	ducc.position[0] += ducc.speed[0]
	ducc.position[1] += ducc.speed[1]
	ducc.mutex.Unlock()
}

func (ducc *Ducc) die() {
	ducc.alive = false
	ducc.speed = []float32{0, 10}
}

func RunDucc(ducc *Ducc, id int, channel chan int) {
	for ; ducc.position[0] >= 0 && ducc.position[0] <= 1080 && ducc.position[1] >= 0 && ducc.position[1] <= 720; {
		_ = wait(20, 20)
		ducc.move()
	}
	channel <- id
}