import Products from "./components/Product.tsx";
import Payment from "./components/Payment.tsx";


export default function App() {
    return (
        <div style={{maxWidth: '600px', margin: '0 auto', padding: '20px'}}>
            <Products/>
            <Payment/>
        </div>
    );
}
