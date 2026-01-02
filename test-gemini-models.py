import requests
import json

# Replace with your actual API key
API_KEY = "AIzaSyDXE_MwMvAoQushsJhHV9gelE9qjYeYTUs"

# Test different API endpoints and models
tests = [
    {
        "name": "List Models (v1beta)",
        "url": f"https://generativelanguage.googleapis.com/v1beta/models?key={API_KEY}",
        "method": "GET"
    },
    {
        "name": "List Models (v1)",
        "url": f"https://generativelanguage.googleapis.com/v1/models?key={API_KEY}",
        "method": "GET"
    },
    {
        "name": "Test gemini-1.5-flash",
        "url": f"https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key={API_KEY}",
        "method": "POST",
        "data": {
            "contents": [{
                "parts": [{"text": "Hello"}]
            }]
        }
    },
    {
        "name": "Test gemini-pro",
        "url": f"https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent?key={API_KEY}",
        "method": "POST",
        "data": {
            "contents": [{
                "parts": [{"text": "Hello"}]
            }]
        }
    }
]

print("=" * 80)
print("GEMINI API MODEL TESTING")
print("=" * 80)

for test in tests:
    print(f"\n{'=' * 80}")
    print(f"Testing: {test['name']}")
    print(f"URL: {test['url'][:100]}...")
    print("-" * 80)
    
    try:
        if test['method'] == 'GET':
            response = requests.get(test['url'], timeout=10)
        else:
            response = requests.post(
                test['url'],
                headers={"Content-Type": "application/json"},
                json=test.get('data', {}),
                timeout=10
            )
        
        print(f"Status Code: {response.status_code}")
        
        if response.status_code == 200:
            print("✅ SUCCESS!")
            try:
                data = response.json()
                if 'models' in data:
                    print(f"Available Models: {len(data.get('models', []))}")
                    for model in data.get('models', [])[:5]:  # Show first 5
                        print(f"  - {model.get('name', 'N/A')}")
                else:
                    print(f"Response: {json.dumps(data, indent=2)[:500]}")
            except:
                print(f"Response: {response.text[:500]}")
        else:
            print(f"❌ FAILED!")
            print(f"Error: {response.text[:500]}")
            
    except Exception as e:
        print(f"❌ EXCEPTION: {str(e)}")

print("\n" + "=" * 80)
print("TESTING COMPLETE")
print("=" * 80)
