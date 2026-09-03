export type OrderStatus = "NEW" | "PROCESSING" | "COMPLETED" | "FAILED";

export interface OrderStatusHistoryEntry {
  orderId: string;
  status: OrderStatus;
  note: string | null;
  dateTime: string;
}

// GET /api/v1/orders returns statusHistory: null for every row; only GET /api/v1/orders/{id} fills it in.
export interface OrderDto {
  orderId: string;
  status: OrderStatus;
  amount: number;
  statusHistory: OrderStatusHistoryEntry[] | null;
}

export interface CreateOrderRequest {
  clientId: string;
  amount: number;
}

export class ApiError extends Error {}

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";

async function parseErrorMessage(response: Response): Promise<string> {
  try {
    const body = await response.json();
    if (body?.message) {
      return body.message as string;
    }
  } catch {
    // no JSON body
  }
  return `Запит відхилено (${response.status})`;
}

export async function fetchOrders(): Promise<OrderDto[]> {
  let response: Response;
  try {
    response = await fetch(`${API_BASE_URL}/api/v1/orders`);
  } catch {
    throw new ApiError("Не вдалося з'єднатися з сервером");
  }
  if (!response.ok) {
    throw new ApiError(await parseErrorMessage(response));
  }
  return response.json();
}

export async function fetchOrder(id: string): Promise<OrderDto> {
  let response: Response;
  try {
    response = await fetch(`${API_BASE_URL}/api/v1/orders/${id}`);
  } catch {
    throw new ApiError("Не вдалося з'єднатися з сервером");
  }
  if (!response.ok) {
    throw new ApiError(await parseErrorMessage(response));
  }
  return response.json();
}

export async function createOrder(payload: CreateOrderRequest): Promise<void> {
  let response: Response;
  try {
    response = await fetch(`${API_BASE_URL}/api/v1/orders`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "X-Idempotency-Key": crypto.randomUUID(),
      },
      body: JSON.stringify(payload),
    });
  } catch {
    throw new ApiError("Не вдалося з'єднатися з сервером");
  }
  if (!response.ok) {
    throw new ApiError(await parseErrorMessage(response));
  }
}
