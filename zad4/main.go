package main

import (
	"github.com/labstack/echo/v4"
	"github.com/labstack/echo/v4/middleware"
	"zad4/controllers"
	"zad4/db"
)

func main() {
	db.InitDatabase()

	e := echo.New()
	e.Use(middleware.Logger())
	e.Use(middleware.Recover())

	api := e.Group("/api")
	controllers.RegisterProductRoutes(api)

	e.Logger.Fatal(e.Start(":8080"))
}
