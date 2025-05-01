import { useState, useEffect } from 'react';

interface Product {
    id: number;
    name: string;
    description: string;
    price: number;
}

export default function Products() {
    const [products, setProducts] = useState<Product[]>([]);

    useEffect(() => {
        fetch('http://localhost:8080/products')
        .then(res => res.json())
        .then((data: Product[]) => setProducts(data))
        .catch(err => console.error('Error fetching products:', err));
    }, []);

    return (
        <section>
            <h1>Product List</h1>
            <ul style={{ listStyle: 'none', padding: 0 }}>
                {products.map((p) => (
                    <li
                        key={p.id}
                        style={{
                            border: '1px solid #ccc',
                            borderRadius: '4px',
                            padding: '10px',
                            marginBottom: '10px'
                        }}
                    >
                        <h2>{p.name}</h2>
                        <p>{p.description}</p>
                        <p><strong>${p.price.toFixed(2)}</strong></p>
                    </li>
                ))}
            </ul>
        </section>
    );
}
