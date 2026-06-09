import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { ApiService } from '../../services/api.service';



interface Tontine {

  id:number;
  nomTontine:string;
  montant:number;
  idAdmin?:number;
  prenomAdmin?:string;
  nomAdmin?:string;
  telephoneAdmin?:string;
  region?:string;

}

@Component({
  selector: 'app-paiement',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './paiement.html',
  styleUrls: ['./paiement.scss']
})


export class Paiement implements OnInit {
  
  tontine:Tontine | null = null;
  selectedMethod:string='';
  phoneNumber:string='';
  isLoading: boolean=false;
  errorMessage:string='';

  constructor(private router: Router,private route:ActivatedRoute,private apiService:ApiService){}

  async ngOnInit() {
    const tontineId=this.route.snapshot.paramMap.get('id');

    if(!tontineId){
      this.errorMessage='Tontine non spécifiéds';
      this.isLoading=false;
      return ;
    }

    await this.loadTontineData(parseInt(tontineId));

  }

  private async loadTontineData(id:number){
    this.isLoading=true;

    // vérifier le cache localStorage

    const cached = localStorage.getItem(`tontine_${id}`);
    if(cached){
      try{
        this.tontine=JSON.parse(cached);
        console.log('Données chargées depuis le cache');
      }
      catch(e){
        console.error('Erreur parsing cache:',e);
      }
}

//rafraichir depuis l'API (données mis a jour)

try{
  const freshData = await this.apiService.get<Tontine>(`/tontine/${id}`);
  this.tontine=freshData;
   //mettre à jour le cache

   localStorage.setItem(`tontine_${id}`,JSON.stringify(freshData));
   console.log("Données rafraichies depuis l'API");
}catch(error){
  console.error('Erreur API :',error);
}

// si l'API échoue mais qu'on a du cache on garde le cache

if(!this.tontine){
  this.errorMessage="impossible de charger les informations de la tontine";
  this.isLoading=false;
}

  }
}