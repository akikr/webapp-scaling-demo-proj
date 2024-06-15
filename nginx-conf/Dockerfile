# Set the base-image
FROM nginx:1.24.0-alpine-slim@sha256:5893dc08a2cb01e21592ff469346ebaacf49167fbc949f45e1c29111981b0427
# Copy custom nginx.conf 
COPY nginx.conf .
# Replace the base-image nginx.conf with custom nginx.conf
RUN mv nginx.conf /etc/nginx/nginx.conf
# Expose the port configured in custom-nginx.conf
EXPOSE 8000
