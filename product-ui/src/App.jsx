import { useEffect, useMemo, useState } from "react";

const apiBaseUrl = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/api/products";

const initialForm = {
  name: "",
  description: "",
  image: ""
};

function App() {
  const [products, setProducts] = useState([]);
  const [form, setForm] = useState(initialForm);
  const [editingId, setEditingId] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const isEditing = useMemo(() => editingId !== null, [editingId]);

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
      ...form
    };

    const endpoint = isEditing ? `${apiBaseUrl}/product/${editingId}` : `${apiBaseUrl}/product`;
    const method = isEditing ? "PUT" : "POST";

    try {
      const response = await fetch(endpoint, {
        method,
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload)
      });
      if (!response.ok) {
        throw new Error(`Failed to ${isEditing ? "update" : "create"} product.`);
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
      image: product.image || ""
    });
    setEditingId(product.id);
  }

  async function handleDelete(id) {
    setError("");
    try {
      const response = await fetch(`${apiBaseUrl}/product/${id}`, {
        method: "DELETE"
      });
      if (!response.ok) {
        throw new Error("Failed to delete product.");
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
