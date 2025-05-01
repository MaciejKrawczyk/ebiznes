package controllers

import (
	"net/http"
	"strconv"

	"github.com/labstack/echo/v4"
	"zad4/db"
	"zad4/models"
)

func RegisterProductRoutes(g *echo.Group) {
	g.GET("/products", GetProducts)
	g.POST("/products", CreateProduct)
	g.GET("/products/:id", GetProduct)
	g.PUT("/products/:id", UpdateProduct)
	g.DELETE("/products/:id", DeleteProduct)
}

func GetProducts(c echo.Context) error {
	var products []models.Product
	db.DB.Preload("Category").Find(&products)
	return c.JSON(http.StatusOK, products)
}

func CreateProduct(c echo.Context) error {
	p := new(models.Product)
	if err := c.Bind(p); err != nil {
		return c.JSON(http.StatusBadRequest, echo.Map{"error": err.Error()})
	}
	if err := db.DB.Create(&p).Error; err != nil {
		return c.JSON(http.StatusInternalServerError, echo.Map{"error": err.Error()})
	}
	return c.JSON(http.StatusCreated, p)
}

func GetProduct(c echo.Context) error {
	id, _ := strconv.Atoi(c.Param("id"))
	var p models.Product
	if err := db.DB.Preload("Category").First(&p, id).Error; err != nil {
		return c.JSON(http.StatusNotFound, echo.Map{"error": "Product not found"})
	}
	return c.JSON(http.StatusOK, p)
}

func UpdateProduct(c echo.Context) error {
	id, _ := strconv.Atoi(c.Param("id"))
	var p models.Product
	if err := db.DB.First(&p, id).Error; err != nil {
		return c.JSON(http.StatusNotFound, echo.Map{"error": "Product not found"})
	}
	if err := c.Bind(&p); err != nil {
		return c.JSON(http.StatusBadRequest, echo.Map{"error": err.Error()})
	}
	db.DB.Save(&p)
	return c.JSON(http.StatusOK, p)
}

func DeleteProduct(c echo.Context) error {
	id, _ := strconv.Atoi(c.Param("id"))
	if err := db.DB.Delete(&models.Product{}, id).Error; err != nil {
		return c.JSON(http.StatusInternalServerError, echo.Map{"error": err.Error()})
	}
	return c.NoContent(http.StatusNoContent)
}
