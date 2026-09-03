import { Routes, Route, Navigate } from "react-router-dom";
import OrdersListPage from "./pages/OrdersListPage";
import NewOrderPage from "./pages/NewOrderPage";
import OrderDetailPage from "./pages/OrderDetailPage";

export default function App() {
  return (
    <Routes>
      <Route path="/" element={<OrdersListPage />} />
      <Route path="/new" element={<NewOrderPage />} />
      <Route path="/orders/:id" element={<OrderDetailPage />} />
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}
