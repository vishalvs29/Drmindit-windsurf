
# CLOUD-COMMUNITY-MANAGER: MANIFESTO

> [!WARNING]
> This file is maintained by cloud-community-manager. 
> DO NOT EDIT - CHANGES WILL BE LOST.

# ccm-scg-rmsfd
As defined in [rmsfd.json](https://github.com/krogertechnology/ccm-community-scg/blob/main/projects/rmsfd.json)

***

## Workspaces

<details>  
<summary>:blue_book: dev (click to expand)</summary>

### `dev` Workspace:
Terraform Directory: [./terraform/workspaces/dev](https://github.com/krogertechnology/ccm-scg-rmsfd/tree/main/terraform/workspaces/dev)

#### :closed_lock_with_key: Provisioner Service Principal
This is the service principal that executes your terraform configuration. The access it has, is the extent of what your terraform configuration can do. By default, it has `Kroger Custom Contributor` and `Key Vault Administrator` to your resource groups.

[sp-10871-ccm-scg-rmsfd-dev](https://portal.azure.com/#view/Microsoft_AAD_IAM/ManagedAppMenuBlade/~/Overview/objectId/48d720ff-7ab6-4df2-8a87-e1933c0c97cf/appId/c6ec93ec-24c0-4106-80a7-6021ad8caa75/preferredSingleSignOnMode~/null/servicePrincipalType/Application/fromNav/) 
 * Object ID: 48d720ff-7ab6-4df2-8a87-e1933c0c97cf
 * Client ID: c6ec93ec-24c0-4106-80a7-6021ad8caa75

#### :file_folder: Resource Groups

 * supplychainnonprod/[rg-ccm-scg-rmsfd-dev-eus2](https://portal.azure.com/?feature.msaljs=true#@kproductivity.onmicrosoft.com/resource/subscriptions/0e06fee6-b7f3-4194-979a-e16195abcbfa/resourceGroups/rg-ccm-scg-rmsfd-dev-eus2/overview)

#### :gear: Managed Service Principals (Auxiliary)

[sp-15113-ccm-scg-rmsfd-dev-deployer](https://portal.azure.com/#view/Microsoft_AAD_IAM/ManagedAppMenuBlade/~/Overview/objectId/92ad1da7-3d02-4559-a707-be4db6e382df/appId/fc5db401-4a3d-4043-92c5-9cdb3d2b1ecb/preferredSingleSignOnMode~/null/servicePrincipalType/Application/fromNav/)

 * Object ID: 92ad1da7-3d02-4559-a707-be4db6e382df
 * Client ID: fc5db401-4a3d-4043-92c5-9cdb3d2b1ecb
 * Managed Role Assignments: 
     * `Kroger Custom Contributor` to `/subscriptions/0e06fee6-b7f3-4194-979a-e16195abcbfa/resourceGroups/rg-ccm-scg-rmsfd-dev-eus2`
  
#### :link: Foreign Service Principal Role Assignments

<ins>236f5c5a-4a00-42e9-9a2d-49033c228de7</ins>
 * Managed Role Assignments: 
     * `Kroger Custom Contributor` to `/subscriptions/0e06fee6-b7f3-4194-979a-e16195abcbfa/resourceGroups/rg-ccm-scg-rmsfd-dev-eus2`
  
#### :key: IAM Groups


Reader Group: [gAZ6920RmsFdReader](https://portal.azure.com/#view/Microsoft_AAD_IAM/GroupDetailsMenuBlade/~/Overview/groupId/0ae212e4-1524-49e6-87cf-c714ec0c5334)
* Object ID: 0ae212e4-1524-49e6-87cf-c714ec0c5334
* Managed Role Assignments:
  * 'Reader'



PIM Group: [gAZ6920RmsFdContributor](https://portal.azure.com/#view/Microsoft_AAD_IAM/GroupDetailsMenuBlade/~/Overview/groupId/656a43ed-d83a-4d8e-ae71-a2d1be44f2a4)
* Object ID: 656a43ed-d83a-4d8e-ae71-a2d1be44f2a4
* Managed Role Assignments:
  * 'Key Vault Administrator'
  * 'Kroger Custom Contributor'



#### :page_with_curl: Template _tfconfig.tf

This file was automatically placed in the workspace on project init, but will no longer be maintained by CCM. We provide a 'latest revision' of the _tfconfig file in the code block below. Use the template below if you ever need to restore to original configuration.

```hcl

# Terraform version is managed in GitHub Actions Workflow files.
terraform {
  required_version = ">= 1.7.5"

  required_providers {

    azurerm = {
      source  = "hashicorp/azurerm"
      version = "~> 3.116.0"
    }

  }

  backend "azurerm" {
    container_name       = "ccm-scg-rmsfd-dev"
    key                  = "env-dev.tfstate"
    resource_group_name  = "rg-cloudops-bootstrap-eastus2"
    storage_account_name = "krlzcmscgeu2"
    subscription_id      = "2912a3d7-4fae-4252-9f75-670d4c28b63a"
    tenant_id            = "8331e14a-9134-4288-bf5a-5e2c8412f074"
    use_oidc             = true
    use_azuread_auth     = true
  }
}

# supplychainnonprod
provider "azurerm" {

  subscription_id = "0e06fee6-b7f3-4194-979a-e16195abcbfa"
  features {}
}



```
#### :page_with_curl: Template data.tf

While CCM does not automatically generate this file in your workspace, you may opt to copy + paste this block into your configuration to reference your existing resource groups.

```hcl

data "azurerm_resource_group" "dev_eastus2" {

  name     = "rg-ccm-scg-rmsfd-dev-eus2"
}


```

</details>


<details>  
<summary>:blue_book: stage (click to expand)</summary>

### `stage` Workspace:
Terraform Directory: [./terraform/workspaces/stage](https://github.com/krogertechnology/ccm-scg-rmsfd/tree/main/terraform/workspaces/stage)

#### :closed_lock_with_key: Provisioner Service Principal
This is the service principal that executes your terraform configuration. The access it has, is the extent of what your terraform configuration can do. By default, it has `Kroger Custom Contributor` and `Key Vault Administrator` to your resource groups.

[sp-10871-ccm-scg-rmsfd-stage](https://portal.azure.com/#view/Microsoft_AAD_IAM/ManagedAppMenuBlade/~/Overview/objectId/2ce1a75e-aa2b-4cc8-b02e-28bc5a568fc8/appId/2a741171-1daf-4978-aeb9-f8053e8f9bd3/preferredSingleSignOnMode~/null/servicePrincipalType/Application/fromNav/) 
 * Object ID: 2ce1a75e-aa2b-4cc8-b02e-28bc5a568fc8
 * Client ID: 2a741171-1daf-4978-aeb9-f8053e8f9bd3

#### :file_folder: Resource Groups

 * supplychainnonprod/[rg-ccm-scg-rmsfd-stage-eus2](https://portal.azure.com/?feature.msaljs=true#@kproductivity.onmicrosoft.com/resource/subscriptions/0e06fee6-b7f3-4194-979a-e16195abcbfa/resourceGroups/rg-ccm-scg-rmsfd-stage-eus2/overview)

#### :gear: Managed Service Principals (Auxiliary)

[sp-15113-ccm-scg-rmsfd-stage-deployer](https://portal.azure.com/#view/Microsoft_AAD_IAM/ManagedAppMenuBlade/~/Overview/objectId/8e2aaab0-f681-40f3-84a1-65cc7e37d3b9/appId/9f2536a8-de0a-44fa-b834-7c6fcb27d265/preferredSingleSignOnMode~/null/servicePrincipalType/Application/fromNav/)

 * Object ID: 8e2aaab0-f681-40f3-84a1-65cc7e37d3b9
 * Client ID: 9f2536a8-de0a-44fa-b834-7c6fcb27d265
 * Managed Role Assignments: 
     * `Kroger Custom Contributor` to `/subscriptions/0e06fee6-b7f3-4194-979a-e16195abcbfa/resourceGroups/rg-ccm-scg-rmsfd-stage-eus2`
  
#### :link: Foreign Service Principal Role Assignments

<ins>236f5c5a-4a00-42e9-9a2d-49033c228de7</ins>
 * Managed Role Assignments: 
     * `Kroger Custom Contributor` to `/subscriptions/0e06fee6-b7f3-4194-979a-e16195abcbfa/resourceGroups/rg-ccm-scg-rmsfd-stage-eus2`
  
#### :key: IAM Groups


Reader Group: [gAZ6920RmsFdReader](https://portal.azure.com/#view/Microsoft_AAD_IAM/GroupDetailsMenuBlade/~/Overview/groupId/0ae212e4-1524-49e6-87cf-c714ec0c5334)
* Object ID: 0ae212e4-1524-49e6-87cf-c714ec0c5334
* Managed Role Assignments:
  * 'Reader'



PIM Group: [gAZ6920RmsFdContributor](https://portal.azure.com/#view/Microsoft_AAD_IAM/GroupDetailsMenuBlade/~/Overview/groupId/656a43ed-d83a-4d8e-ae71-a2d1be44f2a4)
* Object ID: 656a43ed-d83a-4d8e-ae71-a2d1be44f2a4
* Managed Role Assignments:
  * 'Key Vault Administrator'
  * 'Kroger Custom Contributor'



#### :page_with_curl: Template _tfconfig.tf

This file was automatically placed in the workspace on project init, but will no longer be maintained by CCM. We provide a 'latest revision' of the _tfconfig file in the code block below. Use the template below if you ever need to restore to original configuration.

```hcl

# Terraform version is managed in GitHub Actions Workflow files.
terraform {
  required_version = ">= 1.7.5"

  required_providers {

    azurerm = {
      source  = "hashicorp/azurerm"
      version = "~> 3.116.0"
    }

  }

  backend "azurerm" {
    container_name       = "ccm-scg-rmsfd-stage"
    key                  = "env-stage.tfstate"
    resource_group_name  = "rg-cloudops-bootstrap-eastus2"
    storage_account_name = "krlzcmscgeu2"
    subscription_id      = "2912a3d7-4fae-4252-9f75-670d4c28b63a"
    tenant_id            = "8331e14a-9134-4288-bf5a-5e2c8412f074"
    use_oidc             = true
    use_azuread_auth     = true
  }
}

# supplychainnonprod
provider "azurerm" {

  subscription_id = "0e06fee6-b7f3-4194-979a-e16195abcbfa"
  features {}
}



```
#### :page_with_curl: Template data.tf

While CCM does not automatically generate this file in your workspace, you may opt to copy + paste this block into your configuration to reference your existing resource groups.

```hcl

data "azurerm_resource_group" "stage_eastus2" {

  name     = "rg-ccm-scg-rmsfd-stage-eus2"
}


```

</details>



