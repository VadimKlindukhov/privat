import { useCallback, useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { fetchOrders, ApiError, type OrderDto } from "../api";
import { STATUS_LABELS } from "../statusLabels";

export default function OrdersListPage() {
  const navigate = useNavigate();
  const [orders, setOrders] = useState<OrderDto[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const load = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      setOrders(await fetchOrders());
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Невідома помилка");
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    load();
  }, [load]);

  return (
    <main className="page">
      <div className="card card--wide">
        <div className="table-header">
          <h1>Заявки</h1>
          <div className="table-actions">
            <button type="button" onClick={() => navigate("/new")}>
              Додати заявку
            </button>
            <button type="button" className="button-secondary" onClick={load} disabled={loading}>
              {loading ? "Оновлення..." : "Оновити"}
            </button>
          </div>
        </div>

        {error && (
          <p className="message message--error" role="alert">
            {error}
          </p>
        )}

        <div className="table-wrapper">
          <table>
            <thead>
              <tr>
                <th>ID заявки</th>
                <th>Сума, грн</th>
                <th>Статус</th>
              </tr>
            </thead>
            <tbody>
              {orders.length === 0 && !loading && (
                <tr>
                  <td colSpan={3} className="table-empty">
                    Заявок поки немає
                  </td>
                </tr>
              )}
              {orders.map((o) => (
                <tr key={o.orderId} className="table-row--clickable" onClick={() => navigate(`/orders/${o.orderId}`)}>
                  <td>{o.orderId}</td>
                  <td>{Number(o.amount).toFixed(2)}</td>
                  <td>
                    <span className={`status status--${o.status.toLowerCase()}`}>{STATUS_LABELS[o.status]}</span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </main>
  );
}
