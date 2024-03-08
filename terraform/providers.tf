terraform {
  required_providers {
    genesyscloud = {
     source = "mypurecloud/genesyscloud"
    }

     azurerm = {
      source  = "hashicorp/azurerm"
      version = "=3.0.0"
    }

     aws = {
      version = ">= 3.12"
      source  = "hashicorp/aws"
    }
  }
}

provider "aws" {
  region = var.aws_region
}