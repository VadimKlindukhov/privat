import type { OrderStatus } from "./api";

export const STATUS_LABELS: Record<OrderStatus, string> = {
  NEW: "Нова",
  PROCESSING: "В обробці",
  COMPLETED: "Виконана",
  FAILED: "Помилка",
};
