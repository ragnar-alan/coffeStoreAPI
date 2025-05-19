/**
 * Products Management JavaScript
 * Handles AJAX calls to the ProductController endpoints
 */

$(document).ready(function() {
    // Global variables
    let currentProductId = null;
    let isEditMode = false;
    
    // Load products and popular items when the page loads
    loadProducts();
    loadPopularItems();
    
    // Refresh products when the refresh button is clicked
    $('#refreshProducts').on('click', function() {
        loadProducts();
        loadPopularItems();
    });
    
    // Add product button
    $('#addProductBtn').on('click', function() {
        resetProductForm();
        isEditMode = false;
        $('#productModalLabel').text('Add Product');
        $('#productModal').modal('show');
    });
    
    // Save product button
    $('#saveProductBtn').on('click', function() {
        saveProduct();
    });
    
    // Delete product confirmation
    $('#confirmDeleteProductBtn').on('click', function() {
        const productId = currentProductId;
        if (!productId) return;
        
        $.ajax({
            url: `/api/v1/admin/products/${productId}`,
            type: 'DELETE',
            success: function() {
                $('#deleteProductModal').modal('hide');
                showNotification('Success', 'Product deleted successfully.', 'success');
                loadProducts();
            },
            error: function(xhr) {
                showNotification('Error', 'Failed to delete product.', 'error');
            }
        });
    });
    
    /**
     * Loads all products from the API and populates the products table
     */
    function loadProducts() {
        $('#loadingProducts').removeClass('d-none');
        $('#noProducts').addClass('d-none');
        $('#productsTableBody').empty();
        
        $.ajax({
            url: '/api/v1/admin/products/list',
            type: 'GET',
            success: function(response) {
                $('#loadingProducts').addClass('d-none');
                console.log(response);
                if (response && response.length > 0) {
                    response.forEach(function(product) {
                        const row = `
                            <tr>
                                <td>${product.id}</td>
                                <td>${product.product_name}</td>
                                <td>${product.type}</td>
                                <td>€${(product.price_in_cents / 100).toFixed(2)}</td>
                                <td class="action-buttons">
                                    <button class="btn btn-sm btn-warning edit-product" data-product-id="${product.id}">
                                        <i class="fas fa-edit"></i>
                                    </button>
                                    <button class="btn btn-sm btn-danger delete-product" data-product-id="${product.id}">
                                        <i class="fas fa-trash"></i>
                                    </button>
                                </td>
                            </tr>
                        `;
                        $('#productsTableBody').append(row);
                    });
                    
                    // Attach event handlers to the newly created buttons
                    attachEventHandlers();
                } else {
                    $('#noProducts').removeClass('d-none');
                }
            },
            error: function(xhr) {
                $('#loadingProducts').addClass('d-none');
                $('#noProducts').removeClass('d-none').text('Error loading products. Please try again.');
                showNotification('Error', 'Failed to load products.', 'error');
            }
        });
    }
    
    /**
     * Loads the most popular items from the API
     */
    function loadPopularItems() {
        $.ajax({
            url: '/api/v1/admin/products/most-popular',
            type: 'GET',
            success: function(response) {
                if (response) {
                    // Update most popular drink
                    if (response.mostPopularDrink) {
                        $('#popularDrink').html(`
                            <p class="mb-0">${response.mostPopularDrink}</p>
                            <small class="text-muted">Ordered ${response.mostPopularDrinkCount} times</small>
                        `);
                    } else {
                        $('#popularDrink').html('<p class="mb-0">No data available</p>');
                    }
                    
                    // Update most popular topping
                    if (response.mostPopularTopping) {
                        $('#popularTopping').html(`
                            <p class="mb-0">${response.mostPopularTopping}</p>
                            <small class="text-muted">Ordered ${response.mostPopularToppingCount} times</small>
                        `);
                    } else {
                        $('#popularTopping').html('<p class="mb-0">No data available</p>');
                    }
                } else {
                    $('#popularDrink').html('<p class="mb-0">No data available</p>');
                    $('#popularTopping').html('<p class="mb-0">No data available</p>');
                }
            },
            error: function(xhr) {
                $('#popularDrink').html('<p class="mb-0">Error loading data</p>');
                $('#popularTopping').html('<p class="mb-0">Error loading data</p>');
                showNotification('Error', 'Failed to load popular items.', 'error');
            }
        });
    }
    
    /**
     * Attaches event handlers to the product action buttons
     */
    function attachEventHandlers() {
        // Edit product button
        $('.edit-product').on('click', function() {
            const productId = $(this).data('product-id');
            currentProductId = productId;
            isEditMode = true;
            $.ajax({
                url: `/api/v1/admin/products/${productId}`,
                type: 'GET',
                success: function(response) {
                    populateProductForm(response);
                    $('#productModalLabel').text('Edit Product');
                    $('#productModal').modal('show');
                },
                error: function(xhr) {
                    showNotification('Error', 'Failed to load product details.', 'error');
                }
            });
        });
        
        // Delete product button
        $('.delete-product').on('click', function() {
            const productId = $(this).data('product-id');
            currentProductId = productId;
            $('#deleteProductModal').modal('show');
        });
    }
    
    /**
     * Populates the product form with product data
     * @param {Object} product - The product object
     */
    function populateProductForm(product) {
        $('#productId').val(product.id);
        $('#productName').val(product.product_name);
        $('#productType').val(product.type);
        $('#productPrice').val((product.price_in_cents / 100).toFixed(2));
    }
    
    /**
     * Resets the product form
     */
    function resetProductForm() {
        $('#productId').val('');
        $('#productName').val('');
        $('#productType').val('');
        $('#productPrice').val('');
        currentProductId = null;
    }
    
    /**
     * Saves the product (create or update)
     */
    function saveProduct() {
        const productId = $('#productId').val();
        const name = $('#productName').val();
        const type = $('#productType').val();
        const priceInEuros = parseFloat($('#productPrice').val());
        
        // Validate the form
        if (!name) {
            showNotification('Error', 'Product name is required.', 'error');
            return;
        }
        
        if (!type) {
            showNotification('Error', 'Product type is required.', 'error');
            return;
        }
        
        if (isNaN(priceInEuros) || priceInEuros <= 0) {
            showNotification('Error', 'Please enter a valid price.', 'error');
            return;
        }
        
        // Convert price from euros to cents
        const priceInCents = Math.round(priceInEuros * 100);
        
        // Prepare the request data
        const requestData = {
            name: name,
            type: type,
            priceInCents: priceInCents
        };
        
        if (isEditMode) {
            // Update existing product
            $.ajax({
                url: `/api/v1/admin/products/${productId}`,
                type: 'PATCH',
                contentType: 'application/json',
                data: JSON.stringify(requestData),
                success: function(response) {
                    $('#productModal').modal('hide');
                    showNotification('Success', 'Product updated successfully.', 'success');
                    loadProducts();
                },
                error: function(xhr) {
                    let errorMessage = 'Failed to update product.';
                    if (xhr.responseJSON && xhr.responseJSON.message) {
                        errorMessage = xhr.responseJSON.message;
                    }
                    showNotification('Error', errorMessage, 'error');
                }
            });
        } else {
            // Create new product
            $.ajax({
                url: '/api/v1/admin/products',
                type: 'POST',
                contentType: 'application/json',
                data: JSON.stringify(requestData),
                success: function(response) {
                    $('#productModal').modal('hide');
                    showNotification('Success', 'Product created successfully.', 'success');
                    loadProducts();
                },
                error: function(xhr) {
                    let errorMessage = 'Failed to create product.';
                    if (xhr.responseJSON && xhr.responseJSON.message) {
                        errorMessage = xhr.responseJSON.message;
                    }
                    showNotification('Error', errorMessage, 'error');
                }
            });
        }
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
});