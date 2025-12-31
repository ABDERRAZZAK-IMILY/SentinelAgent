import cv2
import numpy as np

def extract_iris_features_from_frame(frame):
    gray = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)
    eye_cascade = cv2.CascadeClassifier(cv2.data.haarcascades + 'haarcascade_eye.xml')
    eyes = eye_cascade.detectMultiScale(gray, scaleFactor=1.1, minNeighbors=5)

    if len(eyes) == 0:
        return None
    
    x, y, w, h = eyes[0]
    eye_region = gray[y:y+h, x:x+w]
    eye_region = cv2.resize(eye_region, (100, 100))
    features = eye_region.flatten()
    return features / 255.0
