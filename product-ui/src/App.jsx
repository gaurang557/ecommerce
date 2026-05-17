import { useEffect, useMemo, useState } from "react";

const apiBaseUrl = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/api/products";

const initialForm = {
  name: "",
  description: "",
  image: "",
  category: "",
  price: "",
  stock: ""
};

function App() {
  const [products, setProducts] = useState([]);
  const [form, setForm] = useState(initialForm);
  const [editingId, setEditingId] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [token, setToken] = useState(() => localStorage.getItem("adminToken") || "");

  const isEditing = useMemo(() => editingId !== null, [editingId]);

  function authHeaders(extra = {}) {
    return token ? { ...extra, Authorization: `Bearer ${token}` } : extra;
  }

  function saveToken(value) {
    setToken(value);
    localStorage.setItem("adminToken", value);
  }

  async function fetchProducts() {
    setLoading(true);
    setError("");
    try {
      const response = await fetch(apiBaseUrl);
      if (!response.ok) {
        throw new Error("Failed to fetch products.");
      }
      const data = await response.json();
      setProducts(Array.isArray(data) ? data : []);
    } catch (err) {
      setError(err.message || "Something went wrong.");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    fetchProducts();
  }, []);

  function updateForm(key, value) {
    setForm((prev) => ({ ...prev, [key]: value }));
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setError("");

    const payload = {
      name: form.name,
      description: form.description,
      image: form.image,
      category: form.category,
      price: form.price === "" ? 0 : Number(form.price),
      stock: form.stock === "" ? 0 : Number(form.stock)
    };

    const endpoint = isEditing ? `${apiBaseUrl}/product/${editingId}` : `${apiBaseUrl}/product`;
    const method = isEditing ? "PUT" : "POST";

    try {
      const response = await fetch(endpoint, {
        method,
        headers: authHeaders({ "Content-Type": "application/json" }),
        body: JSON.stringify(payload)
      });
      if (!response.ok) {
        throw new Error(
          response.status === 401 || response.status === 403
            ? "Admin token required or invalid."
            : `Failed to ${isEditing ? "update" : "create"} product.`
        );
      }
      setForm(initialForm);
      setEditingId(null);
      await fetchProducts();
    } catch (err) {
      setError(err.message || "Request failed.");
    }
  }

  function handleEdit(product) {
    setForm({
      name: product.name || "",
      description: product.description || "",
      image: product.image || "",
      category: product.category || "",
      price: product.price ?? "",
      stock: product.stock ?? ""
    });
    setEditingId(product.id);
  }

  async function handleDelete(id) {
    setError("");
    try {
      const response = await fetch(`${apiBaseUrl}/product/${id}`, {
        method: "DELETE",
        headers: authHeaders()
      });
      if (!response.ok) {
        throw new Error(
          response.status === 401 || response.status === 403
            ? "Admin token required or invalid."
            : "Failed to delete product."
        );
      }
      if (editingId === id) {
        setForm(initialForm);
        setEditingId(null);
      }
      await fetchProducts();
    } catch (err) {
      setError(err.message || "Delete failed.");
    }
  }

  return (
    <div className="container">
      <h1>Product Management</h1>

      <section className="card">
        <h2>Admin Access</h2>
        <label>
          Admin JWT (from POST /api/auth/login with an ADMIN account)
          <input
            value={token}
            placeholder="Paste admin token to enable create/update/delete"
            onChange={(event) => saveToken(event.target.value)}
          />
        </label>
      </section>

      <form className="card" onSubmit={handleSubmit}>
        <h2>{isEditing ? "Edit Product" : "Add Product"}</h2>
        <label>
          Name
          <input
            required
            value={form.name}
            onChange={(event) => updateForm("name", event.target.value)}
          />
        </label>
        <label>
          Description
          <textarea
            required
            value={form.description}
            onChange={(event) => updateForm("description", event.target.value)}
          />
        </label>
        <label>
          Category
          <input
            value={form.category}
            onChange={(event) => updateForm("category", event.target.value)}
          />
        </label>
        <label>
          Price
          <input
            type="number"
            min="0"
            step="0.01"
            value={form.price}
            onChange={(event) => updateForm("price", event.target.value)}
          />
        </label>
        <label>
          Stock
          <input
            type="number"
            min="0"
            step="1"
            value={form.stock}
            onChange={(event) => updateForm("stock", event.target.value)}
          />
        </label>
        <label>
          Image URL
          <input
            value={form.image}
            onChange={(event) => updateForm("image", event.target.value)}
          />
        </label>
        <div className="actions">
          <button type="submit">{isEditing ? "Update" : "Create"}</button>
          {isEditing && (
            <button
              type="button"
              className="muted"
              onClick={() => {
                setEditingId(null);
                setForm(initialForm);
              }}
            >
              Cancel
            </button>
          )}
        </div>
      </form>

      <section className="card">
        <h2>Products</h2>
        {error && <p className="error">{error}</p>}
        {loading ? (
          <p>Loading...</p>
        ) : products.length === 0 ? (
          <p>No products found.</p>
        ) : (
          <div className="product-grid">
            {products.map((product) => (
              <article key={product.id} className="product-card">
                <h3>{product.name}</h3>
                <p>{product.description}</p>
                <p className="meta">
                  {product.category && <span>{product.category} · </span>}
                  ${Number(product.price ?? 0).toFixed(2)} · {product.stock ?? 0} in stock
                </p>
                {product.image && (
                  <a href={product.image} target="_blank" rel="noreferrer">
                    Open Image
                  </a>
                )}
                <div className="actions">
                  <button type="button" onClick={() => handleEdit(product)}>
                    Edit
                  </button>
                  <button
                    type="button"
                    className="danger"
                    onClick={() => handleDelete(product.id)}
                  >
                    Delete
                  </button>
                </div>
              </article>
            ))}
          </div>
        )}
      </section>
    </div>
  );
}

export default App;
