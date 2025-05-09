import React from "react";
import { TakeOutDining } from "./TakeOutDining";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "TakeOutDining",
  component: TakeOutDining,
};

export const Default = () => <TakeOutDining />;
export const Fill = () => <TakeOutDining fill="blue" />;
export const Size = () => <TakeOutDining height="50" width="50" />;
export const CustomStyle = () => <TakeOutDining style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <TakeOutDining className="custom-class" />;
export const Clickable = () => <TakeOutDining onClick={()=>console.log("clicked")} />;

const Template = (args) => <TakeOutDining {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};
