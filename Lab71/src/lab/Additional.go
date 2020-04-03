package lab

import (
	"fmt"
	"math/rand"
	"time"
)

func wait(min int, max int) error {
	if max < min {
		return fmt.Errorf("max (%v) should be equal to or greater than min (%v)", max, min)
	}
	if max == min {
		<-time.After(time.Millisecond * time.Duration(min))
	} else {
		<-time.After(time.Millisecond * time.Duration(rand.Intn(max-min)+min))
	}
	return nil
}

func staticCounter() (f func() int){
	var i int
	f = func() int {
		i++
		return i
	}
	return
}