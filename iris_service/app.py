from flask import Flask, jsonify, request
from flask_socketio import SocketIO, emit
from iris_utils import extract_iris_features_from_frame, detect_eye_position
import numpy as np
import cv2
import os
import requests

app = Flask(__name__)
socketio = SocketIO(app, cors_allowed_origins="*")

# Threshold for authentication (higher = more strict)
THRESHOLD = 0.85
FEATURES_FILE = 'stored_features.npy'

# Spring Boot backend configuration
SPRING_BOOT_URL = os.environ.get('SPRING_BOOT_URL', 'http://localhost:8080')
IRIS_SERVICE_SECRET = os.environ.get('IRIS_SERVICE_SECRET', 'InternalSecretKeyForIrisService_998877')

@socketio.on('detect_eye')
def handle_detect_eye(data):
    """Detect eye position for live preview"""
    import base64
    import io
    from PIL import Image

    try:
        image_bytes = base64.b64decode(data)
        image = Image.open(io.BytesIO(image_bytes))
        frame = cv2.cvtColor(np.array(image), cv2.COLOR_RGB2BGR)

        result = detect_eye_position(frame)
        if result is None:
            emit('eye_position', {'detected': False})
        else:
            face_box, eye_box, eye_image_b64 = result
            emit('eye_position', {
                'detected': True,
                'face': {
                    'x': int(face_box[0]),
                    'y': int(face_box[1]),
                    'width': int(face_box[2]),
                    'height': int(face_box[3])
                },
                'eye': {
                    'x': int(eye_box[0]),
                    'y': int(eye_box[1]),
                    'width': int(eye_box[2]),
                    'height': int(eye_box[3])
                },
                'eyeImage': eye_image_b64
            })
    except Exception as e:
        emit('eye_position', {'detected': False, 'error': str(e)})

@socketio.on('iris_enroll')
def handle_iris_enroll(data):
    """Handle iris enrollment/registration"""
    import base64
    import io
    from PIL import Image

    try:
        image_bytes = base64.b64decode(data)
        image = Image.open(io.BytesIO(image_bytes))
        frame = cv2.cvtColor(np.array(image), cv2.COLOR_RGB2BGR)

        features = extract_iris_features_from_frame(frame)
        if features is None:
            emit('enroll_result', {'success': False, 'error': 'No eye detected. Please position your eye correctly.'})
            return

        # Save the features
        np.save(FEATURES_FILE, features)
        emit('enroll_result', {'success': True, 'message': 'Iris enrolled successfully!'})
    except Exception as e:
        emit('enroll_result', {'success': False, 'error': str(e)})

@socketio.on('iris_frame')
def handle_iris_frame(data):
    """Handle iris authentication"""
    import base64
    import io
    from PIL import Image

    try:
        # Check if enrolled features exist
        if not os.path.exists(FEATURES_FILE):
            emit('iris_result', {'authenticated': False, 'error': 'No iris enrolled. Please enroll first.'})
            return

        # Extract username from data if it's a dict, otherwise use default
        username = 'admin'
        image_data = data
        if isinstance(data, dict):
            username = data.get('username', 'admin')
            image_data = data.get('image')

        image_bytes = base64.b64decode(image_data)
        image = Image.open(io.BytesIO(image_bytes))
        frame = cv2.cvtColor(np.array(image), cv2.COLOR_RGB2BGR)

        features = extract_iris_features_from_frame(frame)
        if features is None:
            emit('iris_result', {'authenticated': False, 'error': 'No eye detected'})
            return

        # Load stored features and compare
        stored_features = np.load(FEATURES_FILE)
        similarity = np.dot(features, stored_features) / (np.linalg.norm(features) * np.linalg.norm(stored_features))
        authenticated = bool(similarity >= THRESHOLD)

        if authenticated:
            # Call Spring Boot backend to get JWT token
            try:
                response = requests.post(
                    f'{SPRING_BOOT_URL}/api/auth/iris-login',
                    json={
                        'username': username,
                        'apiKey': IRIS_SERVICE_SECRET
                    },
                    headers={'Content-Type': 'application/json'},
                    timeout=10
                )
                
                if response.status_code == 200:
                    token_data = response.json()
                    emit('iris_result', {
                        'authenticated': True,
                        'similarity': float(similarity),
                        'token': token_data.get('token'),
                        'username': username
                    })
                else:
                    emit('iris_result', {
                        'authenticated': True,
                        'similarity': float(similarity),
                        'error': 'Iris verified but failed to get auth token from backend',
                        'backendError': response.text
                    })
            except requests.RequestException as e:
                emit('iris_result', {
                    'authenticated': True,
                    'similarity': float(similarity),
                    'error': f'Iris verified but backend connection failed: {str(e)}'
                })
        else:
            emit('iris_result', {'authenticated': False, 'similarity': float(similarity)})
    except Exception as e:
        emit('iris_result', {'authenticated': False, 'error': str(e)})

@socketio.on('check_enrollment')
def check_enrollment():
    """Check if an iris is already enrolled"""
    enrolled = os.path.exists(FEATURES_FILE)
    emit('enrollment_status', {'enrolled': enrolled})

if __name__ == '__main__':
    socketio.run(app, host='0.0.0.0', port=5000)
