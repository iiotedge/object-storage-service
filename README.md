🚀 IoTMining Object Storage Service

A modern, multi-tenant microservice for storing and retrieving device files (snapshots, configs, firmware, logs, etc.) using MinIO (S3-compatible) and PostgreSQL metadata.
No dependency on external device/profile APIs: simply use tenantId, deviceId, and relevant metadata.
🌐 Architecture Overview

    Spring Boot REST API

    MinIO for scalable object storage (S3 API)

    PostgreSQL for device file metadata (multi-tenant, indexed)

    Owns all metadata: Each file record includes tenantId, deviceId, objectType, category, timestamp, etc.

    No REST calls to device/profile services (for performance/isolation)

    Foldering: /tenantId/deviceId/category/objectType/yyyy/MM/dd/filename.ext

    Presigned URLs for secure, scalable direct access

⚡️ Environment Setup
1. Prerequisites

   Java 17+

   Docker/Docker Compose (for MinIO, PostgreSQL)

   Spring Boot 3.x project with JPA and MinIO Java SDK

2. Example docker-compose.yml (MinIO + Postgres only)

services:
minio:
image: minio/minio:latest
container_name: minio
environment:
MINIO_ROOT_USER: minioadmin
MINIO_ROOT_PASSWORD: minioadmin
command: server /data --console-address ":9001"
ports:
- "9000:9000"
- "9001:9001"
volumes:
- minio-data:/data
postgres:
image: postgres:15
container_name: postgres
environment:
POSTGRES_USER: postgres
POSTGRES_PASSWORD: postgres
POSTGRES_DB: iotmining
ports:
- "5432:5432"
volumes:
- pg-data:/var/lib/postgresql/data

volumes:
minio-data:
pg-data:

3. Example application.yml for Spring Boot

minio:
url: http://localhost:9000
access-key: minioadmin
secret-key: minioadmin
bucket: iotmining-objects

spring:
datasource:
url: jdbc:postgresql://localhost:5432/iotmining
username: postgres
password: postgres
jpa:
hibernate:
ddl-auto: update
show-sql: true

📚 API Endpoints & Usage
1. Upload a File

POST /api/objects/upload
Param	Type	Required	Description
tenantId	String	Yes	Tenant unique ID
deviceId	String	Yes	Device unique ID
objectType	String	Yes	snapshot, config, etc.
category	String	Yes	CAMERA, FAN, etc.
file	File	Yes	File to upload

Example:

curl -X POST http://localhost:8101/api/objects/upload \
-F "tenantId=tenantA" \
-F "deviceId=device123" \
-F "objectType=snapshot" \
-F "category=CAMERA" \
-F "file=@/path/to/image.jpg"

Returns:

{
"id": 12,
"tenantId": "tenantA",
"deviceId": "device123",
"objectType": "snapshot",
"category": "CAMERA",
"filename": "image.jpg",
"objectUrl": "tenantA/device123/CAMERA/snapshot/2025/07/21/image.jpg",
"size": 12345,
"uploadedAt": "2025-07-21T14:05:00"
}

2. Download File by Object ID

GET /api/objects/download/{objectId}?tenantId=...

Example:

curl "http://localhost:8101/api/objects/download/12?tenantId=tenantA" --output img.jpg

    Returns the file stream; 404 if not found.

3. Download Latest File by Type

GET /api/objects/download/latest?tenantId=...&deviceId=...&objectType=...

Example:

curl "http://localhost:8101/api/objects/download/latest?tenantId=tenantA&deviceId=device123&objectType=snapshot" --output latest.jpg

    Returns the most recent file of the given type for the device/tenant.

4. Download Latest File by Category (Stream or Presigned URL)

GET /api/objects/download/latest-by-category?tenantId=...&deviceId=...&category=...&objectType=...&presigned=...
Param	Required	Default	Description
tenantId	Yes		Tenant ID
deviceId	Yes		Device ID
category	Yes		CAMERA, FAN, etc.
objectType	No		snapshot, config, etc.
presigned	No	false	true: return a presigned MinIO URL
expiry	No	300	Expiry for presigned URL (in seconds)

As file stream:

curl "http://localhost:8101/api/objects/download/latest-by-category?tenantId=tenantA&deviceId=device123&category=CAMERA" --output cat.jpg

As presigned URL:

curl "http://localhost:8101/api/objects/download/latest-by-category?tenantId=tenantA&deviceId=device123&category=CAMERA&presigned=true"

Returns:

{ "url": "https://minio-url/tenantA/device123/CAMERA/snapshot/..." }

5. Get Latest Presigned URL by Type

GET /api/objects/latest/presigned?tenantId=...&deviceId=...&objectType=...&expiry=...

Example:

curl "http://localhost:8101/api/objects/latest/presigned?tenantId=tenantA&deviceId=device123&objectType=snapshot"

Returns:

{ "url": "https://minio-url/tenantA/device123/CAMERA/snapshot/..." }

📂 Example MinIO Folder Structure

iotmining-objects/
tenantA/
device123/
CAMERA/
snapshot/
2025/07/21/image.jpg

🧪 Testing Checklist

    Use cURL or Postman for all endpoints above.

    Browse MinIO console at http://localhost:9001 to verify file structure.

    Check device_objects table in PostgreSQL for metadata records.

    For presigned URLs, open them in browser—should work directly!

🚦 Production Notes

    All endpoints require tenantId for security/isolation.

    Presigned URLs are valid for a short period (default: 5 minutes).

    Use strong credentials for MinIO in production.

    Add Spring Security (JWT, OAuth2) for real-world deployments.

💡 Extension Ideas

    Add list/search endpoints with paging and filtering.

    Add object delete endpoint.

    Integrate RBAC for per-tenant/device access control.

    Add lifecycle management/cleanup policies.

Need more features? Just open an issue or contact the maintainer!