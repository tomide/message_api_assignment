# Http4s Message Service
## Local Setup
1. Build Docker image\
`docker build -t messageservice .`
2. Tag local Docker repo\
`docker tag messageservice localhost:5000/messageservice`
3. If you do not have a registry yet then run this command. else skip to next step
`docker run -d -p 5000:5000 --restart=always --name registry registry:2`
4. Push to local Docker repo\
`docker push localhost:5000/messageservice`
5. If you have not created an outside network yet, then run command. else skip to next step
`docker network create outside`
4. Start MessageService\
`docker-compose up`

Now you can hit our endpoint at `localhost:8080/create_message`