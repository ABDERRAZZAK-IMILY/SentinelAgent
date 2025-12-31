from flask import Flask, jsonify
from flask_socketio import SocketIO, emit
from iris_utils import extract_iris_features_from_frame
import numpy as np
import cv2

app = Flask(__name__)
socketio = SocketIO(app, cors_allowed_origins="*")

# Threshold for authentication
THRESHOLD = 0.8

@socketio.on('iris_frame')
def handle_iris_frame(data):
    import base64
    import io
    from PIL import Image

    image_bytes = base64.b64decode(data)
    image = Image.open(io.BytesIO(image_bytes))
    frame = cv2.cvtColor(np.array(image), cv2.COLOR_RGB2BGR)

    features = extract_iris_features_from_frame(frame)
    if features is None:
        emit('iris_result', {'authenticated': False, 'error': 'No eye detected'})
        return

   # simple similarity check against stored features
    stored_features = np.load('stored_features.npy')
    similarity = np.dot(features, stored_features) / (np.linalg.norm(features) * np.linalg.norm(stored_features))
    authenticated = similarity >= THRESHOLD

    emit('iris_result', {'authenticated': authenticated, 'similarity': float(similarity)})

if __name__ == '__main__':
    socketio.run(app, host='0.0.0.0', port=5000)
