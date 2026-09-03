import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { fetchOrder, ApiError, type OrderDto } from "../api";
import { STATUS_LABELS } from "../statusLabels";

export default function OrderDetailPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [order, setOrder] = useState<OrderDto | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!id) {
      return;
    }
    setLoading(true);
    setError(null);
    fetchOrder(id)
      .then(setOrder)
      .catch((err) => setError(err instanceof ApiError ? err.message : "Невідома помилка"))
      .finally(() => setLoading(false));
  }, [id]);

  return (
    <main className="page">
      <div className="card card--wide">
        <div className="table-header">
          <h1>Заявка</h1>
          <button type="button" className="button-secondary" onClick={() => navigate("/")}>
            Назад
          </button>
        </div>

        {loading && <p>Завантаження...</p>}

        {error && (
          <p className="message message--error" role="alert">
            {error}
          </p>
        )}

        {order && (
          <>
            <dl className="order-summary">
              <dt>ID заявки</dt>
              <dd>{order.orderId}</dd>

              <dt>Сума, грн</dt>
              <dd>{Number(order.amount).toFixed(2)}</dd>

              <dt>Статус</dt>
              <dd>
                <span className={`status status--${order.status.toLowerCase()}`}>{STATUS_LABELS[order.status]}</span>
              </dd>
            </dl>

            <h2>Історія статусів</h2>
            <div className="table-wrapper">
              <table>
                <thead>
                  <tr>
                    <th>Статус</th>
                    <th>Дата й час</th>
                    <th>Примітка</th>
                  </tr>
                </thead>
                <tbody>
                  {(!order.statusHistory || order.statusHistory.length === 0) && (
                    <tr>
                      <td colSpan={3} className="table-empty">
                        Історія відсутня
                      </td>
                    </tr>
                  )}
                  {order.statusHistory?.map((h, idx) => (
                    <tr key={idx}>
                      <td>
                        <span className={`status status--${h.status.toLowerCase()}`}>{STATUS_LABELS[h.status]}</span>
                      </td>
                      <td>{new Date(h.dateTime).toLocaleString("uk-UA")}</td>
                      <td>{h.note ?? "—"}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </>
        )}
      </div>
    </main>
  );
}
