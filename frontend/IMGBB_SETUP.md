# ImgBB API Setup Instructions

## Getting Your ImgBB API Key

1. **Visit ImgBB API Website**
   - Go to https://api.imgbb.com/

2. **Sign Up / Login**
   - Click on "Get API Key" or "Sign Up"
   - Create an account or login if you already have one

3. **Get Your API Key**
   - After logging in, you'll see your API key on the dashboard
   - Copy the API key

4. **Configure the Application**
   - Open `frontend/src/environments/environment.ts`
   - Your API key is already configured:
   ```typescript
   imgbbApiKey: '9c862141b3c163e8a8e11109ce021ff3'
   ```
   - ✅ No changes needed - already set up!

5. **For Production**
   - Also update `frontend/src/environments/environment.prod.ts` with your production API key

## API Features

- **Free Tier**: 
  - Unlimited image uploads
  - Up to 32 MB per image
  - No bandwidth limits

- **Supported Formats**:
  - JPG, PNG, GIF, BMP, TIFF, WebP

- **Upload Methods**:
  - Binary file upload
  - Base64 encoded data
  - Image URL

## How It Works in the Application

When you upload an image file as a claim attachment:

1. The application detects if the file is an image
2. If it's an image, it uploads to ImgBB first
3. ImgBB returns a permanent URL for the image
4. The claim is created with the ImgBB URL
5. If ImgBB upload fails, it falls back to local file storage

## Benefits

- **Permanent Storage**: Images are stored permanently on ImgBB
- **Fast CDN**: Images are served via ImgBB's CDN for fast loading
- **No Server Storage**: Reduces load on your backend server
- **Reliable**: ImgBB has high uptime and reliability

## Troubleshooting

If image uploads fail:
- Check that your API key is correct
- Ensure the image is under 32 MB
- Check your internet connection
- The app will automatically fall back to local storage if ImgBB fails

## API Documentation

For more details, visit: https://api.imgbb.com/
