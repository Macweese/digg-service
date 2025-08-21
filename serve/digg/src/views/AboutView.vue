<template>
  <div class="about">
    <h1>This is an about page</h1>
  </div>

</template>

<style>

.container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 20px;
}

.controls {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 2rem;
    padding: 1rem;
}

.pagination {
    display: flex;
    gap: 20px;
}

button:hover:not(:disabled) {
    background: #addfff;
}

button:disabled {
    background: #ccc;
}

.customer-table-container {
    overflow: hidden;
    margin-bottom: 2rem;
}

.customer-table {
    width: 100%;
    border-collapse: collapse;
    font-size: 14px;
    text-align: left;
}

.customer-table td {
    padding: 0.5rem;
    vertical-align: top;
}

.customer-table tbody tr {
}

.customer-table tbody tr:nth-child(even) {
    background-color: #f8f8f8;
}

.customer-table tbody tr:hover {
    background-color: #f8f9ff;
    filter: brightness(0.9)
}

.customer-name {
    margin-bottom: 0.1rem;
}

.customer-actions {
    display: flex;
    gap: 8px;
    flex-wrap: wrap;
}

.modal {
    display: none;
    position: fixed;
    z-index: 1000;
    left: 0;
    top: 0;
    width: 100%;
    height: 100%;
    background-color: rgba(0,0,0,0.5);
}

.modal.active {
    display: flex;
    justify-content: center;
    align-items: center;
}

.modal-content {
    background: white;
    padding: 2rem;
    width: 90%;
    max-width: 500px;
    max-height: 80vh;
    overflow-y: auto;
}

.form-group {
    margin-bottom: 1rem;
}

.form-group label {
    display: block;
    margin-bottom: 0.5rem;
    font-weight: 600;
    color: #555;
}

.form-group input {
    width: 95%;
    padding: 7px;
    font-size: 14px;
}

.form-group input:focus {
    outline: none;
}

.form-actions {
    display: flex;
    justify-content: space-between;
    margin-top: 1.5rem;
}

.cancel-btn {
    background: #f0f0f0;
}

.loading {
    text-align: center;
    padding: 2rem;
}

.error {
    background: #f8d7da;
    color: #721c24;
    padding: 1rem;
    margin-bottom: 1rem;
}

.success {
    background: #d4edda;
    color: #155724;
    padding: 1rem;
    margin-bottom: 1rem;
}

.pagination-info {
    color: #666;
    font-size: 0.9rem;
}

@keyframes fadeIn {
    from { opacity: 0; }
    to { opacity: 1; }
}

@keyframes slideIn {
    from { transform: translateY(-50px); opacity: 0; }
    to { transform: translateY(0); opacity: 1; }
}

@media (max-width: 768px) {
    .container {
        padding: 10px;
    }

    .controls {
        flex-direction: column;
        gap: 1rem;
    }

    .header h1 {
        font-size: 2rem;
    }

    .customer-table-container {
        overflow-x: auto;
    }

    .customer-table {
        min-width: 600px;
    }

    .customer-table th,
    .customer-table td {
        padding: 0.5rem;
    }

    .customer-actions {
        flex-direction: column;
    }

    .customer-actions button {
        width: 100%;
        margin-bottom: 4px;
    }
}
</style>
