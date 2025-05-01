package main

import (
	"encoding/json"
	"fmt"
	"log"
	"net/http"
	"sync"
	"time"
)

type Product struct {
	ID          int     `json:"id"`
	Name        string  `json:"name"`
	Description string  `json:"description"`
	Price       float64 `json:"price"`
}

type Payment struct {
	ID        int       `json:"id"`
	Amount    float64   `json:"amount"`
	CardNum   string    `json:"card_num"`
	CVV       string    `json:"cvv"`
	ExpDate   string    `json:"exp_date"`
	Timestamp time.Time `json:"timestamp"`
}

type InMemoryStore struct {
	products      []Product
	payments      []Payment
	mu            sync.RWMutex
	nextPaymentID int
}

var store = &InMemoryStore{
	products: []Product{
		{ID: 1, Name: "Laptop", Description: "High-performance laptop with 16GB RAM", Price: 1299.99},
		{ID: 2, Name: "Smartphone", Description: "Latest model with high-resolution camera", Price: 899.99},
		{ID: 3, Name: "Headphones", Description: "Noise-cancelling wireless headphones", Price: 199.99},
		{ID: 4, Name: "Tablet", Description: "10-inch tablet with retina display", Price: 499.99},
		{ID: 5, Name: "Smart Watch", Description: "Fitness tracking and notifications", Price: 299.99},
	},
	payments:      []Payment{},
	nextPaymentID: 1,
}

func HandleGetProducts(w http.ResponseWriter, r *http.Request) {
	if r.Method != http.MethodGet {
		http.Error(w, "Method not allowed", http.StatusMethodNotAllowed)
		return
	}

	store.mu.RLock()
	defer store.mu.RUnlock()

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(store.products)
}

func HandlePostPayment(w http.ResponseWriter, r *http.Request) {
	if r.Method != http.MethodPost {
		http.Error(w, "Method not allowed", http.StatusMethodNotAllowed)
		return
	}

	var payment Payment
	err := json.NewDecoder(r.Body).Decode(&payment)
	if err != nil {
		http.Error(w, "Invalid payment data", http.StatusBadRequest)
		return
	}

	if payment.Amount <= 0 {
		http.Error(w, "Payment amount must be greater than 0", http.StatusBadRequest)
		return
	}

	if payment.CardNum == "" || payment.CVV == "" || payment.ExpDate == "" {
		http.Error(w, "Missing payment details", http.StatusBadRequest)
		return
	}

	if len(payment.CardNum) > 4 {
		maskedCardNum := "XXXX-XXXX-XXXX-" + payment.CardNum[len(payment.CardNum)-4:]
		payment.CardNum = maskedCardNum
	}

	store.mu.Lock()
	payment.ID = store.nextPaymentID
	store.nextPaymentID++
	payment.Timestamp = time.Now()
	store.payments = append(store.payments, payment)
	store.mu.Unlock()

	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(http.StatusCreated)
	json.NewEncoder(w).Encode(payment)
}

func AddMiddleware(next http.HandlerFunc) http.HandlerFunc {
	const allowedOrigin = "http://localhost:5173"

	return func(w http.ResponseWriter, r *http.Request) {
		origin := r.Header.Get("Origin")
		log.Printf("%s %s (Origin: %s)\n", r.Method, r.URL.Path, origin)

		if origin == allowedOrigin {
			w.Header().Set("Access-Control-Allow-Origin", origin)
			w.Header().Set("Vary", "Origin")
			w.Header().Set("Access-Control-Allow-Credentials", "true")
		}

		w.Header().Set("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
		w.Header().Set("Access-Control-Allow-Headers", "Content-Type, Authorization")

		if r.Method == http.MethodOptions {
			w.WriteHeader(http.StatusNoContent)
			return
		}

		next(w, r)
	}
}

func main() {
	http.HandleFunc("/products", AddMiddleware(HandleGetProducts))
	http.HandleFunc("/payment", AddMiddleware(HandlePostPayment))

	port := 8080
	fmt.Printf("Server starting on port %d...\n", port)
	log.Fatal(http.ListenAndServe(fmt.Sprintf(":%d", port), nil))
}
