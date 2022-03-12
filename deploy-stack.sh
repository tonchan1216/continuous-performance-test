#!/usr/bin/env bash
stack_name="ma-furutanito"
template_path="./infrastructure/cfn.yaml"
parameters="VPCId=vpc-05cd37450a750b709"
parameters+=",IGWId=igw-06fda598abc1e4a21"
parameters+=",VPCCiderBlock=10.2.25.0/24"
parameters+=",Prefix=ma-furutanito"
parameters+=",NeedDataResource=false"
parameters+=",Keypair=MA-furutanito-keypair"
parameters+=",ConsumerContainerImage=cleartone1216/perftest-consumer:latest"
parameters+=",LoadContainerImage=cleartone1216/load-test:latest"

echo ${parameters}

rain deploy ${template_path} ${stack_name} --params ${parameters} -p juku