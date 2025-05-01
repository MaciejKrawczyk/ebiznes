import { useState, ChangeEvent, FormEvent } from 'react';

interface PaymentInfo {
    amount: string;
    card_num: string;
    cvv: string;
    exp_date: string;
}

export default function Payment() {
    const [payment, setPayment] = useState<PaymentInfo>({
        amount: '',
        card_num: '',
        cvv: '',
        exp_date: ''
    });

    const handleChange = (e: ChangeEvent<HTMLInputElement>) => {
        const { name, value } = e.target;
        setPayment(prev => ({ ...prev, [name]: value }));
    };

    const handleCardNumberChange = (e: ChangeEvent<HTMLInputElement>) => {
        const raw = e.target.value.replace(/\D/g, '').slice(0, 16);
        const formatted = raw.match(/.{1,4}/g)?.join(' ') || raw;
        setPayment(prev => ({ ...prev, card_num: formatted }));
    };

    const handleCvvChange = (e: ChangeEvent<HTMLInputElement>) => {
        const raw = e.target.value.replace(/\D/g, '').slice(0, 4);
        setPayment(prev => ({ ...prev, cvv: raw }));
    };

    const handleSubmit = async (e: FormEvent<HTMLFormElement>) => {
        e.preventDefault();
        try {
            const response = await fetch('http://localhost:8080/payment', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    amount: parseFloat(payment.amount),
                    card_num: payment.card_num.replace(/\s+/g, ''),
                    cvv: payment.cvv,
                    exp_date: payment.exp_date
                })
            });

            const data = await response.json();
            alert('Payment successful!\n' + JSON.stringify(data, null, 2));
            setPayment({ amount: '', card_num: '', cvv: '', exp_date: '' });
        } catch (error) {
            console.error('Payment error:', error);
        }
    };

    return (
        <section>
            <h1>Make a Payment</h1>
            <form onSubmit={handleSubmit} style={{ display: 'grid', gap: '10px' }}>
                <div>
                    <label>Amount:</label><br />
                    <input
                        type="number"
                        name="amount"
                        value={payment.amount}
                        onChange={handleChange}
                        step="0.01"
                        required
                    />
                </div>
                <div>
                    <label>Card Number:</label><br />
                    <input
                        type="text"
                        name="card_num"
                        value={payment.card_num}
                        onChange={handleCardNumberChange}
                        placeholder="1234 5678 9012 3456"
                        maxLength={19}
                        required
                    />
                </div>
                <div>
                    <label>CVV:</label><br />
                    <input
                        type="text"
                        name="cvv"
                        value={payment.cvv}
                        onChange={handleCvvChange}
                        placeholder="123"
                        maxLength={4}
                        required
                    />
                </div>
                <div>
                    <label>Expiration Date:</label><br />
                    <input
                        type="month"
                        name="exp_date"
                        value={payment.exp_date}
                        onChange={handleChange}
                        required
                    />
                </div>
                <button type="submit">Pay</button>
            </form>
        </section>
    );
}
