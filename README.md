# Http4s User Service
## Local Setup
1. Build Docker image\
`docker build -t tom-messageservice .`
2. Tag local Docker repo\
`docker tag tom-messageservice localhost:5000/tom-messageservice`
3. Push to local Docker repo\
`docker push localhost:5000/tom-messageservice`
4. Start PostgreSQL and UserService\
`docker-compose up`

Now you can hit our endpoint at `localhost:5000/create_message`