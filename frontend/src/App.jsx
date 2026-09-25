
import { useEffect, useState } from "react";
import "./App.css";

const ORDER_SERVICE_URL = import.meta.env.VITE_ORDER_SERVICE_URL;
const INVENTORY_SERVICE_URL = import.meta.env.VITE_INVENTORY_SERVICE_URL;

function App() {
  const [product, setProduct] = useState("");
  const [quantity, setQuantity] = useState(1);

  const [orders, setOrders] = useState([]);
  const [inventory, setInventory] = useState([]);

  const [message, setMessage] = useState("");
  const [loading, setLoading] = useState(false);
  const [ordersLoading, setOrdersLoading] = useState(true);
  const [inventoryLoading, setInventoryLoading] = useState(true);

  // Store the latest order created by the customer
  const [latestOrderId, setLatestOrderId] = useState(null);

  // Fetch all orders from Order Service
  const fetchOrders = async () => {
    try {
      const response = await fetch(`${ORDER_SERVICE_URL}/api/orders`);

      if (!response.ok) {
        throw new Error("Failed to fetch orders");
      }

      const data = await response.json();
      setOrders(data);
    } catch (error) {
      console.error("Error fetching orders:", error);
    } finally {
      setOrdersLoading(false);
    }
  };

  // Fetch all products from Inventory Service
  const fetchInventory = async () => {
    try {
      const response = await fetch(`${INVENTORY_SERVICE_URL}/api/inventory`);

      if (!response.ok) {
        throw new Error("Failed to fetch inventory");
      }

      const data = await response.json();
      setInventory(data);
    } catch (error) {
      console.error("Error fetching inventory:", error);
    } finally {
      setInventoryLoading(false);
    }
  };

  // Load inventory once when page starts
  useEffect(() => {
    fetchInventory();
  }, []);

  // Load orders and refresh every 2 seconds
  useEffect(() => {
    fetchOrders();

    const interval = setInterval(() => {
      fetchOrders();
    }, 2000);

    return () => clearInterval(interval);
  }, []);

  // Update message when latest order status changes
  useEffect(() => {
    if (latestOrderId === null) {
      return;
    }

    const latestOrder = orders.find(
      (order) => order.id === latestOrderId
    );

    if (!latestOrder) {
      return;
    }

    if (latestOrder.status === "PENDING") {
      setMessage(
        `Order #${latestOrder.id} is being processed...`
      );
    }

    if (latestOrder.status === "CONFIRMED") {
      setMessage(
        `Order #${latestOrder.id} confirmed successfully.`
      );
    }

    if (latestOrder.status === "FAILED") {
      setMessage(
        `Order #${latestOrder.id} could not be processed.`
      );
    }
  }, [orders, latestOrderId]);

  // Create a new order
  const placeOrder = async (e) => {
    e.preventDefault();

    if (!product || Number(quantity) < 1) {
      setMessage(
        "Please select a product and enter a valid quantity."
      );
      return;
    }

    setLoading(true);
    setMessage("");

    try {
      const response = await fetch(`${ORDER_SERVICE_URL}/api/orders`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          product: product,
          quantity: Number(quantity),
        }),
      });

      if (!response.ok) {
        throw new Error("Failed to create order");
      }

      const data = await response.json();

      // Remember the order just created
      setLatestOrderId(data.id);

      // Show processing message initially
      setMessage(
        `Order #${data.id} is being processed...`
      );

      // Clear form
      setProduct("");
      setQuantity(1);

      // Immediately refresh orders
      fetchOrders();

    } catch (error) {
      console.error("Error creating order:", error);

      setMessage(
        "Could not create order. Please make sure the backend is running."
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="app">

      {/* Navbar */}
      <header className="navbar">
        <div>
          <h1>OrderFlow</h1>
          <p>Order Management System</p>
        </div>
      </header>

      <main className="dashboard">

        {/* Create Order */}
        <section className="order-card">
          <h2>Create New Order</h2>

          <form onSubmit={placeOrder}>

            {/* Product */}
            <div className="form-group">
              <label>Product</label>

              <select
                value={product}
                onChange={(e) => setProduct(e.target.value)}
                disabled={inventoryLoading}
              >
                <option value="">
                  {inventoryLoading
                    ? "Loading products..."
                    : "Select a product"}
                </option>

                {inventory.map((item) => (
                  <option
                    key={item.id}
                    value={item.product}
                  >
                    {item.product}
                  </option>
                ))}
              </select>
            </div>

            {/* Quantity */}
            <div className="form-group">
              <label>Quantity</label>

              <input
                type="number"
                min="1"
                value={quantity}
                onChange={(e) => setQuantity(e.target.value)}
              />
            </div>

            {/* Submit */}
            <button
              type="submit"
              disabled={loading || inventoryLoading}
            >
              {loading ? "Placing Order..." : "Place Order"}
            </button>

          </form>

          {/* Order Message */}
          {message && (
            <p className="message">
              {message}
            </p>
          )}
        </section>

        {/* Orders */}
        <section className="orders-card">

          <div className="section-header">
            <h2>Recent Orders</h2>
            <span>Order History</span>
          </div>

          {ordersLoading ? (
            <p>Loading orders...</p>
          ) : orders.length === 0 ? (
            <p>No orders found.</p>
          ) : (
            <table>

              <thead>
                <tr>
                  <th>Order ID</th>
                  <th>Product</th>
                  <th>Quantity</th>
                  <th>Status</th>
                </tr>
              </thead>

              <tbody>

                {orders.map((order) => (
                  <tr key={order.id}>

                    <td>
                      #{order.id}
                    </td>

                    <td>
                      {order.product}
                    </td>

                    <td>
                      {order.quantity}
                    </td>

                    <td>
                      <span
                        className={`status ${
                          order.status === "CONFIRMED"
                            ? "confirmed"
                            : order.status === "FAILED"
                            ? "failed"
                            : "pending"
                        }`}
                      >
                        {order.status}
                      </span>
                    </td>

                  </tr>
                ))}

              </tbody>

            </table>
          )}

        </section>

      </main>

    </div>
  );
}

export default App;
