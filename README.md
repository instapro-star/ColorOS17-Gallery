# ColorOS 17 Gallery ("Photos") - Android Studio / GitHub Actions Project

## Included Features
1. **ColorOS 17 Liquid Glass UI & Floating Dock** (`ColorOSLiquidGlassDock.kt`)
2. **120Hz Spring Animations & Pinch-to-Zoom Grid** (`MainActivity.kt`)
3. **Independent Gallery Theme Lock** (Force White / Force Dark / System Auto)
4. **4K CC (Color Correction & Clarity) GPU Shader Pipeline** (`ProMediaRenderEngine.kt`)
5. **Keyframe Animation Engine** (`KeyframeInterpolator.kt`)
6. **Multi-Layer Photo & Video Overlays (PIP + Screen/Overlay/Multiply Blend Modes)**
7. **On-Device AI Tools** (ML Kit Subject Segmentation Smart Cutout, AI Eraser, OCR)
8. **Custom 4K Launcher Icon** embedded in all `mipmap-*` folders.

## How to Build APK via Termux & GitHub Actions
1. Extract this ZIP file in Termux (`unzip ColorOS17_Gallery_Project.zip -d ColorOS17Gallery`).
2. `cd ColorOS17Gallery`
3. Initialize git and push to your GitHub repository:
   ```bash
   git init
   git add .
   git commit -m "Initial ColorOS 17 Gallery build"
   git branch -M main
   git remote add origin https://github.com/YOUR_USERNAME/YOUR_REPO.git
   git push -u origin main
   ```
4. Open your repository on GitHub -> **Actions** tab -> Download the compiled `ColorOS17-Gallery-Photos-APK` artifact!
