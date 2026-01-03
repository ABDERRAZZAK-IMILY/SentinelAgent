import cv2
import numpy as np

def extract_iris_features_from_frame(frame):
    """
    Extract iris features from a frame.
    Returns None if no valid eye is detected.
    """
    gray = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)
    
    # First detect face to narrow down eye search area
    face_cascade = cv2.CascadeClassifier(cv2.data.haarcascades + 'haarcascade_frontalface_default.xml')
    eye_cascade = cv2.CascadeClassifier(cv2.data.haarcascades + 'haarcascade_eye.xml')
    
    faces = face_cascade.detectMultiScale(gray, scaleFactor=1.1, minNeighbors=5, minSize=(100, 100))
    
    if len(faces) == 0:
        return None
    
    # Get the largest face
    largest_face = max(faces, key=lambda f: f[2] * f[3])
    fx, fy, fw, fh = largest_face
    
    # Search for eyes only in the upper half of the face
    face_upper = gray[fy:fy + fh // 2, fx:fx + fw]
    
    # Detect eyes with stricter parameters
    eyes = eye_cascade.detectMultiScale(
        face_upper, 
        scaleFactor=1.1, 
        minNeighbors=10,  # More strict
        minSize=(30, 30),  # Minimum eye size
        maxSize=(fw // 2, fh // 3)  # Maximum eye size
    )
    
    if len(eyes) == 0:
        return None
    
    # Get the largest eye detected
    largest_eye = max(eyes, key=lambda e: e[2] * e[3])
    ex, ey, ew, eh = largest_eye
    
    # Extract eye region from original gray image
    eye_region = face_upper[ey:ey + eh, ex:ex + ew]
    
    # Validate eye region - check for dark center (pupil)
    if not validate_eye_region(eye_region):
        return None
    
    # Resize and normalize
    eye_region = cv2.resize(eye_region, (100, 100))
    
    # Apply histogram equalization for better feature extraction
    eye_region = cv2.equalizeHist(eye_region)
    
    features = eye_region.flatten()
    return features / 255.0


def validate_eye_region(eye_region):
    """
    Validate if the detected region is actually an eye.
    Checks for the presence of a dark pupil in the center.
    """
    if eye_region is None or eye_region.size == 0:
        return False
    
    h, w = eye_region.shape[:2]
    if h < 20 or w < 20:
        return False
    
    # Check center region for dark pixels (pupil)
    center_x, center_y = w // 2, h // 2
    center_region = eye_region[
        center_y - h // 4:center_y + h // 4,
        center_x - w // 4:center_x + w // 4
    ]
    
    if center_region.size == 0:
        return False
    
    # The center should be significantly darker than the edges
    center_mean = np.mean(center_region)
    edge_region = np.concatenate([
        eye_region[0:h // 4, :].flatten(),
        eye_region[3 * h // 4:, :].flatten()
    ])
    edge_mean = np.mean(edge_region)
    
    # Pupil should be darker than surrounding area
    darkness_ratio = center_mean / (edge_mean + 1)
    
    # Valid eye has darker center
    return darkness_ratio < 0.9


def detect_eye_position(frame):
    """
    Detect eye position and return coordinates for visualization.
    Returns (face_box, eye_box, eye_image_base64) or None if not detected.
    """
    import base64
    
    gray = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)
    
    # Load cascades
    face_cascade = cv2.CascadeClassifier(cv2.data.haarcascades + 'haarcascade_frontalface_default.xml')
    eye_cascade = cv2.CascadeClassifier(cv2.data.haarcascades + 'haarcascade_eye.xml')
    
    # Detect faces
    faces = face_cascade.detectMultiScale(gray, scaleFactor=1.1, minNeighbors=5, minSize=(80, 80))
    
    if len(faces) == 0:
        return None
    
    # Get the largest face
    largest_face = max(faces, key=lambda f: f[2] * f[3])
    fx, fy, fw, fh = largest_face
    
    # Search for eyes only in the upper half of the face
    face_upper = gray[fy:fy + fh // 2, fx:fx + fw]
    face_upper_color = frame[fy:fy + fh // 2, fx:fx + fw]
    
    # Detect eyes
    eyes = eye_cascade.detectMultiScale(
        face_upper, 
        scaleFactor=1.1, 
        minNeighbors=6,
        minSize=(25, 25),
        maxSize=(fw // 2, fh // 3)
    )
    
    if len(eyes) == 0:
        return None
    
    # Get the largest eye
    largest_eye = max(eyes, key=lambda e: e[2] * e[3])
    ex, ey, ew, eh = largest_eye
    
    # Calculate absolute eye position (relative to full frame)
    abs_eye_x = fx + ex
    abs_eye_y = fy + ey
    
    # Extract and encode eye region as base64 for preview
    eye_region_color = face_upper_color[ey:ey + eh, ex:ex + ew]
    
    # Resize for better preview
    eye_preview = cv2.resize(eye_region_color, (150, 150))
    
    # Convert BGR to RGB for proper colors
    eye_preview_rgb = cv2.cvtColor(eye_preview, cv2.COLOR_BGR2RGB)
    
    # Encode as JPEG base64
    _, buffer = cv2.imencode('.jpg', eye_preview_rgb, [cv2.IMWRITE_JPEG_QUALITY, 90])
    eye_b64 = base64.b64encode(buffer).decode('utf-8')
    
    # Return face box, eye box (absolute coords), and eye image
    face_box = (fx, fy, fw, fh)
    eye_box = (abs_eye_x, abs_eye_y, ew, eh)
    
    return face_box, eye_box, eye_b64

