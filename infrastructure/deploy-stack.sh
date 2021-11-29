#!/usr/bin/env bash

if [ "$1" == "network" ]; then
    stack_name="ma-furutanito-network"
    template_path="network.yaml"
    parameters="VPCId=vpc-05cd37450a750b709,VPCCiderBlock=10.2.25.0/24,Prefix=ma-furutanito"
elif [ "$1" == "s3" ]; then
    stack_name="ma-furutanito-s3"
    template_path="s3.yaml"
    parameters="Prefix=ma-furutanito"
elif [ "$1" == "sqs" ]; then
    stack_name="ma-furutanito-sqs"
    template_path="sqs.yaml"
    parameters="Prefix=ma-furutanito"
elif [ "$1" == "dynamodb" ]; then
    stack_name="ma-furutanito-dynamodb"
    template_path="dynamodb.yaml"
    parameters="Prefix=ma-furutanito"
else
    stack_name="ma-furutanito"
    template_path="cfn.yaml"
    parameters="VPCId=vpc-05cd37450a750b709,VPCCiderBlock=10.2.25.0/24,Prefix=ma-furutanito"
fi

echo ${template_path}

if [ "$parameters" == "" ]; then
    rain deploy ${template_path} ${stack_name} -p juku
else
    rain deploy ${template_path} ${stack_name} --params ${parameters} -p juku
fi