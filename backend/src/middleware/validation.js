const logger = require('../utils/logger');

/**
 * Request Validation Middleware
 * Handles schema-based validation with proper error handling
 */

/**
 * Validation rules
 */
const validationRules = {
    email: {
        type: 'email',
        pattern: /^[^\s@]+@[^\s@]+\.[^\s@]+$/,
        message: 'Invalid email format'
    },
    string: {
        type: 'string',
        minLength: 1,
        maxLength: 1000,
        message: 'Invalid string format'
    },
    password: {
        type: 'string',
        minLength: 8,
        maxLength: 128,
        pattern: /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]/,
        message: 'Password must be at least 8 characters long and contain uppercase, lowercase, number, and special character'
    },
    number: {
        type: 'number',
        min: 0,
        message: 'Invalid number format'
    },
    boolean: {
        type: 'boolean',
        message: 'Invalid boolean format'
    },
    array: {
        type: 'array',
        message: 'Invalid array format'
    },
    object: {
        type: 'object',
        message: 'Invalid object format'
    },
    date: {
        type: 'date',
        message: 'Invalid date format'
    },
    uuid: {
        type: 'string',
        pattern: /^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i,
        message: 'Invalid UUID format'
    }
};

/**
 * Validate request data against schema
 */
const validateRequest = (data, schema) => {
    const errors = [];
    const sanitizedData = {};
    
    try {
        // Validate each field in schema
        for (const [fieldName, fieldRules] of Object.entries(schema)) {
            const value = data[fieldName];
            
            // Check if required field is missing
            if (fieldRules.required && (value === undefined || value === null || value === '')) {
                errors.push({
                    field: fieldName,
                    message: `${fieldName} is required`
                });
                continue;
            }
            
            // Skip validation if field is not provided and not required
            if (value === undefined && !fieldRules.required) {
                continue;
            }
            
            // Validate type
            if (fieldRules.type) {
                const typeValidation = validateType(value, fieldRules.type, fieldName);
                if (!typeValidation.isValid) {
                    errors.push({
                        field: fieldName,
                        message: typeValidation.error
                    });
                    continue;
                }
            }
            
            // Validate string constraints
            if (fieldRules.type === 'string' && typeof value === 'string') {
                // Length validation
                if (fieldRules.minLength !== undefined && value.length < fieldRules.minLength) {
                    errors.push({
                        field: fieldName,
                        message: `${fieldName} must be at least ${fieldRules.minLength} characters long`
                    });
                    continue;
                }
                
                if (fieldRules.maxLength !== undefined && value.length > fieldRules.maxLength) {
                    errors.push({
                        field: fieldName,
                        message: `${fieldName} must be no more than ${fieldRules.maxLength} characters long`
                    });
                    continue;
                }
                
                // Pattern validation
                if (fieldRules.pattern && !fieldRules.pattern.test(value)) {
                    errors.push({
                        field: fieldName,
                        message: fieldRules.message || `${fieldName} format is invalid`
                    });
                    continue;
                }
            }
            
            // Validate number constraints
            if (fieldRules.type === 'number' && typeof value === 'number') {
                if (fieldRules.min !== undefined && value < fieldRules.min) {
                    errors.push({
                        field: fieldName,
                        message: `${fieldName} must be at least ${fieldRules.min}`
                    });
                    continue;
                }
                
                if (fieldRules.max !== undefined && value > fieldRules.max) {
                    errors.push({
                        field: fieldName,
                        message: `${fieldName} must be no more than ${fieldRules.max}`
                    });
                    continue;
                }
            }
            
            // Validate enum values
            if (fieldRules.enum && !fieldRules.enum.includes(value)) {
                errors.push({
                    field: fieldName,
                    message: `${fieldName} must be one of: ${fieldRules.enum.join(', ')}`
                });
                continue;
            }
            
            // Sanitize and store valid data
            sanitizedData[fieldName] = sanitizeValue(value, fieldRules);
        }
        
        return {
            isValid: errors.length === 0,
            errors: errors,
            data: sanitizedData
        };
        
    } catch (error) {
        logger.error('Error in request validation', { error: error.message, stack: error.stack });
        return {
            isValid: false,
            errors: [{
                field: 'validation',
                message: 'Validation error occurred'
            }],
            data: {}
        };
    }
};

/**
 * Validate data type
 */
const validateType = (value, expectedType, fieldName) => {
    try {
        switch (expectedType) {
            case 'email':
                return {
                    isValid: validationRules.email.pattern.test(value),
                    error: validationRules.email.message
                };
                
            case 'string':
                return {
                    isValid: typeof value === 'string',
                    error: validationRules.string.message
                };
                
            case 'number':
                return {
                    isValid: typeof value === 'number' && !isNaN(value),
                    error: validationRules.number.message
                };
                
            case 'boolean':
                return {
                    isValid: typeof value === 'boolean',
                    error: validationRules.boolean.message
                };
                
            case 'array':
                return {
                    isValid: Array.isArray(value),
                    error: validationRules.array.message
                };
                
            case 'object':
                return {
                    isValid: typeof value === 'object' && value !== null && !Array.isArray(value),
                    error: validationRules.object.message
                };
                
            case 'date':
                const date = new Date(value);
                return {
                    isValid: !isNaN(date.getTime()),
                    error: validationRules.date.message
                };
                
            case 'uuid':
                return {
                    isValid: validationRules.uuid.pattern.test(value),
                    error: validationRules.uuid.message
                };
                
            default:
                return {
                    isValid: true,
                    error: null
                };
        }
    } catch (error) {
        return {
            isValid: false,
            error: `Type validation failed for ${fieldName}: ${error.message}`
        };
    }
};

/**
 * Sanitize input value
 */
const sanitizeValue = (value, rules) => {
    if (typeof value === 'string') {
        // Remove potential XSS
        let sanitized = value
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#x27;')
            .replace(/\//g, '&#x2F;');
        
        // Trim whitespace
        sanitized = sanitized.trim();
        
        // Apply length limits
        if (rules.maxLength && sanitized.length > rules.maxLength) {
            sanitized = sanitized.substring(0, rules.maxLength);
        }
        
        return sanitized;
    }
    
    if (typeof value === 'number') {
        // Apply numeric constraints
        if (rules.min !== undefined && value < rules.min) {
            return rules.min;
        }
        if (rules.max !== undefined && value > rules.max) {
            return rules.max;
        }
        return value;
    }
    
    if (typeof value === 'boolean') {
        return value;
    }
    
    if (Array.isArray(value)) {
        // Sanitize array elements
        return value.map(item => sanitizeValue(item, rules));
    }
    
    if (typeof value === 'object' && value !== null) {
        // Sanitize object values
        const sanitized = {};
        for (const [key, val] of Object.entries(value)) {
            sanitized[key] = sanitizeValue(val, rules);
        }
        return sanitized;
    }
    
    return value;
};

/**
 * Validate query parameters
 */
const validateQuery = (query, schema) => {
    const sanitizedQuery = {};
    const errors = [];
    
    try {
        for (const [paramName, paramRules] of Object.entries(schema)) {
            const value = query[paramName];
            
            // Parse string values to appropriate types
            let parsedValue = value;
            if (typeof value === 'string') {
                if (paramRules.type === 'number') {
                    parsedValue = parseInt(value, 10);
                    if (isNaN(parsedValue)) {
                        errors.push({
                            field: paramName,
                            message: `${paramName} must be a valid number`
                        });
                        continue;
                    }
                } else if (paramRules.type === 'boolean') {
                    parsedValue = value.toLowerCase() === 'true';
                }
            }
            
            // Apply validation rules
            const validation = validateRequest({ [paramName]: parsedValue }, { [paramName]: paramRules });
            
            if (!validation.isValid) {
                errors.push(...validation.errors);
            } else {
                sanitizedQuery[paramName] = validation.data[paramName];
            }
        }
        
        return {
            isValid: errors.length === 0,
            errors: errors,
            data: sanitizedQuery
        };
        
    } catch (error) {
        logger.error('Error in query validation', { error: error.message, stack: error.stack });
        return {
            isValid: false,
            errors: [{
                field: 'query_validation',
                message: 'Query validation error occurred'
            }],
            data: {}
        };
    }
};

/**
 * Validate file upload
 */
const validateFile = (file, rules) => {
    const errors = [];
    
    try {
        // Check if file exists
        if (!file) {
            if (rules.required) {
                errors.push({
                    field: 'file',
                    message: 'File is required'
                });
            }
            return { isValid: false, errors };
        }
        
        // Validate file size
        if (rules.maxSize && file.size > rules.maxSize) {
            errors.push({
                field: 'file',
                message: `File size must be less than ${Math.round(rules.maxSize / 1024 / 1024)}MB`
            });
        }
        
        // Validate file type
        if (rules.allowedTypes && !rules.allowedTypes.includes(file.mimetype)) {
            errors.push({
                field: 'file',
                message: `File type must be one of: ${rules.allowedTypes.join(', ')}`
            });
        }
        
        // Validate file name
        if (rules.filenamePattern && !rules.filenamePattern.test(file.originalname)) {
            errors.push({
                field: 'file',
                message: 'Filename format is invalid'
            });
        }
        
        return {
            isValid: errors.length === 0,
            errors: errors
        };
        
    } catch (error) {
        logger.error('Error in file validation', { error: error.message, stack: error.stack });
        return {
            isValid: false,
            errors: [{
                field: 'file_validation',
                message: 'File validation error occurred'
            }]
        };
    }
};

module.exports = {
    validateRequest,
    validateQuery,
    validateFile,
    validationRules
};
