export interface WarehouseLocation {
  street: string;
  city: string;
  country: string;
  lat: number;
  lon: number;
}

export interface InventoryItem {
  itemId: string;
  sku: string;
  description: string;
  quantity: number;
  weightKg: number;
  volumeM3: number;
  expirationDate?: string;
}

export interface Warehouse {
  warehouseId: string;
  name: string;
  location: WarehouseLocation;
  maxWeightKg: number;
  maxVolumeM3: number;
  currentWeightKg: number;
  currentVolumeM3: number;
  inventory: InventoryItem[];
  createdAt: string;
}

export interface WarehouseListItem {
  warehouseId: string;
  name: string;
  locationCity: string;
  maxWeightKg: number;
  maxVolumeM3: number;
  currentWeightKg: number;
  currentVolumeM3: number;
  weightFillPercent: number;
  volumeFillPercent: number;
}
