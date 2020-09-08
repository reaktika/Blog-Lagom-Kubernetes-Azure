# Azure setup

Setup account
```
> az account set --subscription "Azure subscription 1"
```

az group list

## ACR
```
> az acr create --resource-group lagom-kubernetes --name cookieregistry --sku Basic
> az acr login --name cookieregistry
```

List ACR login server
```
> az acr list --resource-group lagom-kubernetes --query "[].{acrLoginServer:loginServer}" --output table
```

## AKS
Create AKS cluster:
```
> az aks create --resource-group lagom-kubernetes --name CookieCluster --node-count 1 --attach-acr cookieregistry
```

Add AKS credentials to kubectl
```
> az aks get-credentials --resource-group lagom-kubernetes --name cookiecluster
```

## Event Hub
```
> az eventhubs namespace create --name cookiehub --resource-group lagom-kubernetes -l westeurope
```
```
> az eventhubs eventhub create --resource-group lagom-kubernetes --namespace-name cookiehub --name events.cookie.out --message-retention 1 --partition-count 1
```

Create jaas.conf
```
> az eventhubs namespace authorization-rule list --resource-group lagom-kubernetes --namespace-name cookiehub
> az eventhubs namespace authorization-rule keys list --resource-group lagom-kubernetes --namespace-name cookiehub --name RootManageSharedAccessKey
```

## Postgres
Create server
```
> az postgres server create --location westeurope --resource-group lagom-kubernetes --name cookiepostgres --admin-user cookieadmin --admin-password Cookie123  --sku-name B_Gen5_1
```
Allow access to other Azure services (like AKS)
```
> az postgres server firewall-rule create -g lagom-kubernetes -s cookiepostgres -n "AllowAllWindowsAzureIps" --start-ip-address "0.0.0.0" --end-ip-address "0.0.0.0"
```

Create database
```
> az postgres db create --name cookie_db  --resource-group lagom-kubernetes --server-name cookiepostgres
```

## Ingress setup
```
> az aks show --resource-group lagom-kubernetes --name cookiecluster --query nodeResourceGroup -o tsv
```

```
> az network public-ip create --resource-group MC_lagom-kubernetes_CookieCluster_westeurope --name myAKSPublicIP --sku Standard --allocation-method static --query publicIp.ipAddress -o tsv
OUTPUT_PUBLIC_IP
```

```
> helm install nginx-ingress stable/nginx-ingress \                                 
      --namespace default \
      --set controller.replicaCount=1 \
      --set controller.nodeSelector."beta\.kubernetes\.io/os"=linux \
      --set defaultBackend.nodeSelector."beta\.kubernetes\.io/os"=linux \
      --set controller.service.loadBalancerIP={OUTPUT_PUBLIC_IP} \
      --set controller.service.annotations."service\.beta\.kubernetes\.io/azure-dns-label-name"="cookiemonster"
```