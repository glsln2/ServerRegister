from flask import Flask, request, jsonify
import ddddocr
import base64
import logging

app = Flask(__name__)
ocr = ddddocr.DdddOcr()

logging.basicConfig(level=logging.INFO)  # 配置日志

@app.route('/recognize', methods=['POST'])
def recognize_captcha():
    logging.info("Received request to /recognize")
    try:
        data = request.get_json()
        if not data or 'image_base64' not in data:
            logging.warning("Missing image_base64 in request")
            return jsonify({'error': 'Missing image_base64 in request'}), 400

        image_base64 = data['image_base64']
        image_bytes = base64.b64decode(image_base64)

        logging.info("Decoding image_base64 successful")

        res = ocr.classification(image_bytes)
        logging.info(f"Recognition result: {res}")
        return jsonify({'result': res})

    except Exception as e:
        logging.error(f"Error during recognition: {e}", exc_info=True)
        return jsonify({'error': str(e)}), 500

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=True)