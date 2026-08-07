import React from "react";
import { Whatsapp } from "./Whatsapp";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Whatsapp",
  component: Whatsapp,
};

export const Default = () => <Whatsapp />;
export const Fill = () => <Whatsapp fill="blue" />;
export const Size = () => <Whatsapp height="50" width="50" />;
export const CustomStyle = () => <Whatsapp style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Whatsapp className="custom-class" />;
export const Clickable = () => <Whatsapp onClick={()=>console.log("clicked")} />;

const Template = (args) => <Whatsapp {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};
