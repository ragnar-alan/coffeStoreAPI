/**
 * Orders Management JavaScript
 * Handles AJAX calls to the AdminController endpoints
 */

$(document).ready(function() {
    // Global variables
    let currentOrderNumber = null;
    
    // Load orders when the page loads
    loadOrders();
    
    // Refresh orders when the refresh button is clicked
    $('#refreshOrders').on('click', function() {
        loadOrders();
    });
    
    // Edit order button in the order details modal
    $('#editOrderBtn').on('click', function() {
        const orderNumber = currentOrderNumber;
        if (!orderNumber) return;
        
        // Get the order details and populate the edit form
        $.ajax({
            url: `/api/v1/admin/orders/${orderNumber}`,
            type: 'GET',
            success: function(response) {
                populateEditForm(response);
                $('#orderDetailsModal').modal('hide');
                $('#editOrderModal').modal('show');
            },
            error: function(xhr) {
                showNotification('Error', 'Failed to load order details for editing.', 'error');
            }
        });
    });
    
    // Add order line button in the edit form
    $('#addOrderLineBtn').on('click', function() {
        addOrderLineRow();
    });
    
    // Save order changes button
    $('#saveOrderBtn').on('click', function() {
        saveOrderChanges();
    });
    
    // Delete order confirmation
    $('#confirmDeleteBtn').on('click', function() {
        const orderNumber = currentOrderNumber;
        if (!orderNumber) return;
        
        $.ajax({
            url: `/api/v1/admin/orders/${orderNumber}`,
            type: 'DELETE',
            success: function() {
                $('#deleteConfirmModal').modal('hide');
                showNotification('Success', 'Order deleted successfully.', 'success');
                loadOrders();
            },
            error: function(xhr) {
                showNotification('Error', 'Failed to delete order.', 'error');
            }
        });
    });
    
    /**
     * Loads all orders from the API and populates the orders table
     */
    function loadOrders() {
        $('#loadingOrders').removeClass('d-none');
        $('#noOrders').addClass('d-none');
        $('#ordersTableBody').empty();
        
        $.ajax({
            url: '/api/v1/admin/orders/list',
            type: 'GET',
            success: function(response) {
                $('#loadingOrders').addClass('d-none');
                console.log(response);
                if (response && response.length > 0) {
                    response.forEach(function(order) {
                        const row = `
                            <tr>
                                <td>${order.order_number}</td>
                                <td>${order.orderer}</td>
                                <td><span class="badge bg-${getStatusBadgeClass(order.status)}">${order.status}</span></td>
                                <td>€${(order.total_price_in_cents / 100).toFixed(2)}</td>
                                <td class="action-buttons">
                                    <button class="btn btn-sm btn-info view-order" data-order-number="${order.order_number}">
                                        <i class="fas fa-eye"></i>
                                    </button>
                                    <button class="btn btn-sm btn-warning edit-order" data-order-number="${order.order_number}">
                                        <i class="fas fa-edit"></i>
                                    </button>
                                    <button class="btn btn-sm btn-danger delete-order" data-order-number="${order.order_number}">
                                        <i class="fas fa-trash"></i>
                                    </button>
                                </td>
                            </tr>
                        `;
                        $('#ordersTableBody').append(row);
                    });
                    
                    // Attach event handlers to the newly created buttons
                    attachEventHandlers();
                } else {
                    $('#noOrders').removeClass('d-none');
                }
            },
            error: function(xhr) {
                $('#loadingOrders').addClass('d-none');
                $('#noOrders').removeClass('d-none').text('Error loading orders. Please try again.');
                showNotification('Error', 'Failed to load orders.', 'error');
            }
        });
    }
    
    /**
     * Attaches event handlers to the order action buttons
     */
    function attachEventHandlers() {
        // View order button
        $('.view-order').on('click', function() {
            console.log($(this));
            const orderNumber = $(this).data('orderNumber');
            viewOrderDetails(orderNumber);
        });
        
        // Edit order button
        $('.edit-order').on('click', function() {
            const orderNumber = $(this).data('orderNumber');
            currentOrderNumber = orderNumber;
            
            $.ajax({
                url: `/api/v1/admin/orders/${orderNumber}`,
                type: 'GET',
                success: function(response) {
                    populateEditForm(response);
                    $('#editOrderModal').modal('show');
                },
                error: function(xhr) {
                    showNotification('Error', 'Failed to load order details for editing.', 'error');
                }
            });
        });
        
        // Delete order button
        $('.delete-order').on('click', function() {
            const orderNumber = $(this).data('order-number');
            currentOrderNumber = orderNumber;
            $('#deleteConfirmModal').modal('show');
        });
    }
    
    /**
     * Displays the order details in a modal
     * @param {string} orderNumber - The order number
     */
    function viewOrderDetails(orderNumber) {
        currentOrderNumber = orderNumber;
        
        $.ajax({
            url: `/api/v1/admin/orders/${orderNumber}`,
            type: 'GET',
            success: function(response) {
                const orderDetails = `
                    <div class="row mb-3">
                        <div class="col-md-6">
                            <p><strong>Order Number:</strong> ${response.order_number}</p>
                            <p><strong>Orderer:</strong> ${response.orderer}</p>
                            <p><strong>Status:</strong> <span class="badge bg-${getStatusBadgeClass(response.status)}">${response.status}</span></p>
                        </div>
                        <div class="col-md-6">
                            <p><strong>Subtotal:</strong> €${(response.sub_total_price_in_cents / 100).toFixed(2)}</p>
                            <p><strong>Total:</strong> €${(response.total_price_in_cents / 100).toFixed(2)}</p>
                            <p><strong>Currency:</strong> ${response.currency}</p>
                        </div>
                    </div>
                    
                    <h6>Order Lines</h6>
                    <div class="table-responsive">
                        <table class="table table-sm">
                            <thead>
                                <tr>
                                    <th>Item</th>
                                    <th>Price</th>
                                </tr>
                            </thead>
                            <tbody>
                                ${response.order_lines.map(line => `
                                    <tr>
                                        <td>${getOrderLineDescription(line)}</td>
                                        <td>€${(line.price_in_cents / 100).toFixed(2)}</td>
                                    </tr>
                                `).join('')}
                            </tbody>
                        </table>
                    </div>
                    
                    ${response.discounts && response.discounts.length > 0 ? `
                        <h6>Applied Discounts</h6>
                        <ul class="list-group">
                            ${response.discounts.map(discount => `
                                <li class="list-group-item">
                                    ${discount.name} - 
                                    ${discount.percentage ? `${discount.percentage}%` : `€${(discount.amount_in_cents / 100).toFixed(2)}`}
                                </li>
                            `).join('')}
                        </ul>
                    ` : ''}
                `;
                
                $('#orderDetailsContent').html(orderDetails);
                $('#orderDetailsModal').modal('show');
            },
            error: function(xhr) {
                showNotification('Error', 'Failed to load order details.', 'error');
            }
        });
    }
    
    /**
     * Populates the edit form with order data
     * @param {Object} order - The order object
     */
    function populateEditForm(order) {
        $('#editOrderer').val(order.orderer);
        
        // Clear existing order lines
        $('#orderLinesContainer').empty();
        
        // Add order lines
        if (order.order_lines && order.order_lines.length > 0) {
            order.order_lines.forEach(function(line) {
                addOrderLineRow(line);
            });
        } else {
            addOrderLineRow();
        }
    }
    
    /**
     * Adds a new order line row to the edit form
     * @param {Object} line - Optional order line data to populate the row
     */
    function addOrderLineRow(line = null) {
        const rowId = new Date().getTime();
        const hasDrink = line && line.drink;
        const hasToppings = line && line.toppings && line.toppings.length > 0;
        
        const row = `
            <div class="card mb-2 order-line-row" data-row-id="${rowId}">
                <div class="card-body">
                    <div class="row">
                        <div class="col-md-5">
                            <div class="mb-2">
                                <label class="form-label">Drink</label>
                                <select class="form-select drink-select" name="drink">
                                    <option value="">Select a drink</option>
                                    <option value="CAPPUCCINO" ${hasDrink && line.drink === 'CAPPUCCINO' ? 'selected' : ''}>Cappuccino</option>
                                    <option value="COFFEE" ${hasDrink && line.drink === 'COFFEE' ? 'selected' : ''}>Coffee</option>
                                    <option value="LATTE" ${hasDrink && line.drink === 'LATTE' ? 'selected' : ''}>Latte</option>
                                    <option value="TEA" ${hasDrink && line.drink === 'TEA' ? 'selected' : ''}>Tea</option>
                                </select>
                            </div>
                        </div>
                        <div class="col-md-5">
                            <div class="mb-2">
                                <label class="form-label">Toppings</label>
                                <div class="form-check">
                                    <input class="form-check-input" type="checkbox" value="MILK" id="topping-milk-${rowId}" ${hasToppings && line.toppings.includes('MILK') ? 'checked' : ''}>
                                    <label class="form-check-label" for="topping-milk-${rowId}">Milk</label>
                                </div>
                                <div class="form-check">
                                    <input class="form-check-input" type="checkbox" value="CHOCOLATE" id="topping-chocolate-${rowId}" ${hasToppings && line.toppings.includes('CHOCOLATE') ? 'checked' : ''}>
                                    <label class="form-check-label" for="topping-chocolate-${rowId}">Chocolate</label>
                                </div>
                                <div class="form-check">
                                    <input class="form-check-input" type="checkbox" value="SUGAR" id="topping-sugar-${rowId}" ${hasToppings && line.toppings.includes('SUGAR') ? 'checked' : ''}>
                                    <label class="form-check-label" for="topping-sugar-${rowId}">Sugar</label>
                                </div>
                            </div>
                        </div>
                        <div class="col-md-2">
                            <div class="mb-2">
                                <label class="form-label">Price (€)</label>
                                <input type="number" class="form-control price-input" step="0.01" value="${line ? (line.price_in_cents / 100).toFixed(2) : ''}" required>
                            </div>
                            <button type="button" class="btn btn-sm btn-outline-danger remove-line-btn">
                                <i class="fas fa-trash"></i> Remove
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        `;
        
        $('#orderLinesContainer').append(row);
        
        // Attach event handler to the remove button
        $('.remove-line-btn').off('click').on('click', function() {
            $(this).closest('.order-line-row').remove();
        });
    }
    
    /**
     * Saves the order changes
     */
    function saveOrderChanges() {
        const orderNumber = currentOrderNumber;
        if (!orderNumber) return;
        
        const orderer = $('#editOrderer').val();
        const orderLines = [];
        
        // Collect order lines data
        $('.order-line-row').each(function() {
            const row = $(this);
            const drink = row.find('.drink-select').val();
            const priceInCents = Math.round(parseFloat(row.find('.price-input').val()) * 100);
            
            // Collect selected toppings
            const toppings = [];
            row.find('input[type="checkbox"]:checked').each(function() {
                toppings.push($(this).val());
            });
            
            // Only add the order line if a drink is selected and price is valid
            if (drink && !isNaN(priceInCents)) {
                orderLines.push({
                    drink: drink,
                    toppings: toppings,
                    priceInCents: priceInCents
                });
            }
        });
        
        // Validate the form
        if (!orderer) {
            showNotification('Error', 'Orderer name is required.', 'error');
            return;
        }
        
        if (orderLines.length === 0) {
            showNotification('Error', 'At least one order line is required.', 'error');
            return;
        }
        
        // Prepare the request data
        const requestData = {
            orderer: orderer,
            orderLines: orderLines
        };
        
        // Send the update request
        $.ajax({
            url: `/api/v1/admin/orders/${orderNumber}`,
            type: 'PATCH',
            contentType: 'application/json',
            data: JSON.stringify(requestData),
            success: function(response) {
                $('#editOrderModal').modal('hide');
                showNotification('Success', 'Order updated successfully.', 'success');
                loadOrders();
            },
            error: function(xhr) {
                let errorMessage = 'Failed to update order.';
                if (xhr.responseJSON && xhr.responseJSON.message) {
                    errorMessage = xhr.responseJSON.message;
                }
                showNotification('Error', errorMessage, 'error');
            }
        });
    }
    
    /**
     * Shows a notification toast
     * @param {string} title - The notification title
     * @param {string} message - The notification message
     * @param {string} type - The notification type (success, error, info)
     */
    function showNotification(title, message, type) {
        $('#toastTitle').text(title);
        $('#toastMessage').text(message);
        
        const toast = $('#notificationToast');
        toast.removeClass('bg-success bg-danger bg-info');
        
        switch (type) {
            case 'success':
                toast.addClass('bg-success text-white');
                break;
            case 'error':
                toast.addClass('bg-danger text-white');
                break;
            case 'info':
                toast.addClass('bg-info text-white');
                break;
        }
        
        const bsToast = new bootstrap.Toast(toast);
        bsToast.show();
    }
    
    /**
     * Returns the appropriate Bootstrap badge class for the given order status
     * @param {string} status - The order status
     * @returns {string} The Bootstrap badge class
     */
    function getStatusBadgeClass(status) {
        switch (status) {
            case 'PENDING':
                return 'warning';
            case 'COMPLETED':
                return 'success';
            case 'CANCELLED':
                return 'danger';
            default:
                return 'secondary';
        }
    }
    
    /**
     * Returns a description of the order line
     * @param {Object} line - The order line object
     * @returns {string} The order line description
     */
    function getOrderLineDescription(line) {
        let description = line.drink || 'Unknown Item';
        
        if (line.toppings && line.toppings.length > 0) {
            description += ' with ' + line.toppings.join(', ');
        }
        
        return description;
    }
});