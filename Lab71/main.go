// +build glfw

package main

import (
	"fmt"
	"os"

	"github.com/inkyblackness/imgui-go/v2"

	"Lab71/src/lab"
	"Lab71/src/platforms"
	"Lab71/src/render"
)

func main() {
	context := imgui.CreateContext(nil)
	defer context.Destroy()
	io := imgui.CurrentIO()

	platform, err := platforms.NewGLFW(io, "Lab71", platforms.GLFWClientAPIOpenGL3)
	if err != nil {
		_, _ = fmt.Fprintf(os.Stderr, "%v\n", err)
		os.Exit(-1)
	}
	defer platform.Dispose()

	renderer, err := render.NewOpenGL3(io)
	if err != nil {
		_, _ = fmt.Fprintf(os.Stderr, "%v\n", err)
		os.Exit(-1)
	}
	defer renderer.Dispose()

	lab.Run(platform, renderer)
}
