import React from "react";
import { DeliveryDining } from "./DeliveryDining";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "DeliveryDining",
  component: DeliveryDining,
};

export const Default = () => <DeliveryDining />;
export const Fill = () => <DeliveryDining fill="blue" />;
export const Size = () => <DeliveryDining height="50" width="50" />;
export const CustomStyle = () => <DeliveryDining style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <DeliveryDining className="custom-class" />;

export const Clickable = () => <DeliveryDining onClick={()=>console.log("clicked")} />;

const Template = (args) => <DeliveryDining {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
