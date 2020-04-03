package lab

import (
	"fmt"
	"github.com/inkyblackness/imgui-go/v2"
	"log"
	"math/rand"
	"os"
	"sync"
	"time"
)

const stateLimit = 5

var treeSprite imgui.TextureID
var bushSprite imgui.TextureID
var font       imgui.Font

type Garden struct {
	width         int
	height        int
	trees         [][]int
	gardener      [2]int
	treeMutex     [][]sync.Mutex
	natureMutex   sync.Mutex
	gardenerMutex sync.Mutex
}

func NewGarden(width int, height int) (*Garden, error) {
	if width < 1 || height < 1 {
		return nil, fmt.Errorf("garden dimensions should be at least 1 (got %v, %v)", width, height)
	}
	garden := &Garden{
		width:         width,
		height:        height,
		trees:         make([][]int, height),
		gardener:      [2]int{0, 0},
		treeMutex:     make([][]sync.Mutex, height),
		natureMutex:   sync.Mutex{},
		gardenerMutex: sync.Mutex{},
	}
	for i := 0; i < height; i++ {
		garden.trees[i] = make([]int, width)
		garden.treeMutex[i] = make([]sync.Mutex, width)
		for j := 0; j < width; j++ {
			garden.trees[i][j] = rand.Intn(stateLimit + 1)
			garden.treeMutex[i][j] = sync.Mutex{}
		}
	}
	return garden, nil
}

func InitGarden(r Renderer) {
	treeSprite = r.CreateImageTexture("res/Oak_Sapling.png")
	bushSprite = r.CreateImageTexture("res/Dead_Bush.png")
	font = imgui.CurrentIO().Fonts().AddFontFromFileTTF("res/consola.ttf", 12)
}

func (garden *Garden) UpdateTree(x int, y int) error {
	if x < 0 || x >= garden.width{
		return fmt.Errorf("x should be in range [0, %v) (got %v)", garden.width, x)
	}
	if y < 0 || y >= garden.height{
		return fmt.Errorf("y should be in range [0, %v) (got %v)", garden.height, y)
	}
	garden.natureMutex.Lock()
	garden.treeMutex[y][x].Lock()
	garden.trees[y][x]--
	garden.treeMutex[y][x].Unlock()
	garden.natureMutex.Unlock()
	return nil
}

func (garden *Garden) MoveGardener(x int, y int) error {
	if x < 0 || x >= garden.width{
		return fmt.Errorf("x should be in range [0, %v) (got %v)", garden.width, x)
	}
	if y < 0 || y >= garden.height{
		return fmt.Errorf("y should be in range [0, %v) (got %v)", garden.height, y)
	}
	garden.gardenerMutex.Lock()
	garden.gardener[0] = y
	garden.gardener[1] = x
	garden.gardenerMutex.Unlock()
	garden.treeMutex[y][x].Lock()
	if garden.trees[y][x] <= -stateLimit {
		for ; garden.trees[y][x] < stateLimit; {
			garden.trees[y][x]++
			_ = wait(100, 200)
		}
	}
	_ = wait(200, 400)
	garden.treeMutex[y][x].Unlock()
	return nil
}

func (garden *Garden) Display() {
	garden.natureMutex.Lock()
	garden.gardenerMutex.Lock()
	show := true
	imgui.BeginV("Garden", &show, imgui.WindowFlagsNoTitleBar|imgui.WindowFlagsAlwaysAutoResize)
	for i := 0; i < garden.height; i++ {
		for j := 0; j < garden.width; j++ {
			sprite := treeSprite
			if garden.trees[i][j] <= -stateLimit {
				sprite = bushSprite
			}
			if garden.gardener[0] == i && garden.gardener[1] == j {
				imgui.ImageButton(sprite, imgui.Vec2{X: 43, Y: 43})
			} else {
				imgui.Image(sprite, imgui.Vec2{X: 50, Y: 50})
			}
			imgui.SameLine()
			imgui.PushFont(font)
			imgui.Text(fmt.Sprint(garden.trees[i][j]))
			imgui.PopFont()
			if j < garden.width - 1 {
				imgui.SameLine()
			}
		}
	}
	imgui.End()
	garden.gardenerMutex.Unlock()
	garden.natureMutex.Unlock()
}

func (garden *Garden) Dump(path string) {
	garden.natureMutex.Lock()
	garden.gardenerMutex.Lock()
	dump := fmt.Sprintf("[%v]\r\n", time.Now())
	for i := 0; i < garden.height; i++ {
		for j := 0; j < garden.width; j++ {
			if garden.gardener[0] == i && garden.gardener[1] == j {
				dump += fmt.Sprintf("(%v)", garden.trees[i][j])
			} else {
				dump += fmt.Sprint(garden.trees[i][j])
			}
			if j < garden.width - 1 {
				dump += ","
			} else {
				dump += "\r\n"
			}
		}
	}
	dump += "\r\n"
	f, err := os.OpenFile(path, os.O_APPEND|os.O_CREATE|os.O_WRONLY, 0644)
	if err != nil {
		log.Println(err)
	}
	defer f.Close()
	if _, err := f.WriteString(dump); err != nil {
		log.Println(err)
	}
	garden.gardenerMutex.Unlock()
	garden.natureMutex.Unlock()
}