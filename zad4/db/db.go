package db

import (
	"database/sql"
	sqliteDriver "gorm.io/driver/sqlite"
	"gorm.io/gorm"
	_ "modernc.org/sqlite"
	"zad4/models"
)

var DB *gorm.DB

func InitDatabase() {
	sqlDB, err := sql.Open("sqlite", "app.db")
	if err != nil {
		panic("failed to open sqlite via modernc driver: " + err.Error())
	}

	dialector := sqliteDriver.New(sqliteDriver.Config{
		Conn:       sqlDB,
		DriverName: "sqlite",
	})
	db, err := gorm.Open(dialector, &gorm.Config{})
	if err != nil {
		panic("failed to connect database: " + err.Error())
	}

	err = db.AutoMigrate(
		&models.Product{},
	)
	if err != nil {
		return
	}

	DB = db
}
