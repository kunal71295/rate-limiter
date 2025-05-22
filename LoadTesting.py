import requests
import concurrent.futures
import time

URL = "http://localhost:8080/api/rate-limit"
HEADERS = {
    "Authorization": "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJrdW5hbCJ9.TsxX09st0cohGL6d6bjgyerNdv2rqGB0N4cCzUOes5Y",
    "Cookie": "JSESSIONID=4653D69680872CF2DF5C38C327AD7CD1"
}

TOTAL_REQUESTS = 120
CONCURRENT_WORKERS = 10


def make_request(i):
    try:
        response = requests.get(URL, headers=HEADERS)
        print(f"Request {i}: Status {response.status_code}")
        return response.status_code
    except Exception as e:
        print(f"Request {i} failed: {e}")
        return None


def main():
    start_time = time.time()

    with concurrent.futures.ThreadPoolExecutor(max_workers=CONCURRENT_WORKERS) as executor:
        futures = [executor.submit(make_request, i) for i in range(1, TOTAL_REQUESTS + 1)]
        results = [f.result() for f in concurrent.futures.as_completed(futures)]

    end_time = time.time()

    print("\nSummary:")
    print(f"Total requests: {len(results)}")
    print(f"429 responses: {results.count(429)}")
    print(f"200 responses: {results.count(200)}")
    print(f"Time taken: {end_time - start_time:.2f} seconds")


if __name__ == "__main__":
    main()
