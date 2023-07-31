import React from "react";
import { CardTravel } from "./CardTravel";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "CardTravel",
  component: CardTravel,
};

export const Default = () => <CardTravel />;
export const Fill = () => <CardTravel fill="blue" />;
export const Size = () => <CardTravel height="50" width="50" />;
export const CustomStyle = () => <CardTravel style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <CardTravel className="custom-class" />;

export const Clickable = () => <CardTravel onClick={()=>console.log("clicked")} />;

const Template = (args) => <CardTravel {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
