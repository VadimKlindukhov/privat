import { useState, type FormEvent } from "react";
import { useNavigate } from "react-router-dom";
import { CLIENTS } from "../clients";
import { createOrder, ApiError } from "../api";

const MAX_AMOUNT = 50_000;

export default function NewOrderPage() {
  const navigate = useNavigate();
  const [clientId, setClientId] = useState("");
  const [amount, setAmount] = useState("");
  const [pending, setPending] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError(null);

    const amountValue = Number(amount);

    if (!clientId) {
      setError("Оберіть клієнта");
      return;
    }

    if (!amount || Number.isNaN(amountValue) || amountValue <= 0) {
      setError("Введіть коректну суму більше нуля");
      return;
    }

    if (amountValue > MAX_AMOUNT) {
      setError(`Сума не може перевищувати ${MAX_AMOUNT.toLocaleString("uk-UA")} грн`);
      return;
    }

    setPending(true);
    try {
      await createOrder({ clientId, amount: amountValue });
      navigate("/");
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Невідома помилка");
    } finally {
      setPending(false);
    }
  }

  return (
    <main className="page">
      <div className="card">
        <h2>Нова заявка</h2>
        <form onSubmit={handleSubmit} noValidate>
          <label className="field">
            <span>Клієнт</span>
            <select
              id="clientId"
              name="clientId"
              required
              value={clientId}
              onChange={(e) => setClientId(e.target.value)}
              disabled={pending}
            >
              <option value="" disabled>
                Оберіть клієнта
              </option>
              {CLIENTS.map((c) => (
                <option key={c.id} value={c.id}>
                  {c.label} ({c.id})
                </option>
              ))}
            </select>
          </label>

          <label className="field">
            <span>Сума, грн (максимум {MAX_AMOUNT.toLocaleString("uk-UA")})</span>
            <input
              id="amount"
              name="amount"
              type="number"
              inputMode="decimal"
              min="0.01"
              max={MAX_AMOUNT}
              step="0.01"
              placeholder="0.00"
              required
              value={amount}
              onChange={(e) => setAmount(e.target.value)}
              disabled={pending}
            />
          </label>

          <div className="form-actions">
            <button type="submit" disabled={pending}>
              {pending ? "Відправлення..." : "Відправити"}
            </button>
            <button type="button" className="button-secondary" onClick={() => navigate("/")} disabled={pending}>
              Скасувати
            </button>
          </div>

          {error && (
            <p className="message message--error" role="alert">
              {error}
            </p>
          )}
        </form>
      </div>
    </main>
  );
}
