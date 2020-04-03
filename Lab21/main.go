// +build glfw

package main

import (
	"fmt"
	"os"

	"github.com/inkyblackness/imgui-go/v2"

	"Lab21/src/lab"
	"Lab21/src/platforms"
	"Lab21/src/render"
)

func main() {
	context := imgui.CreateContext(nil)
	defer context.Destroy()
	io := imgui.CurrentIO()

	platform, err := platforms.NewGLFW(io, "Lab21", platforms.GLFWClientAPIOpenGL3)
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
