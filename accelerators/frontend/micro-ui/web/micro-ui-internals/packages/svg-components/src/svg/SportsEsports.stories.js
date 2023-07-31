import React from "react";
import { SportsEsports } from "./SportsEsports";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "SportsEsports",
  component: SportsEsports,
};

export const Default = () => <SportsEsports />;
export const Fill = () => <SportsEsports fill="blue" />;
export const Size = () => <SportsEsports height="50" width="50" />;
export const CustomStyle = () => <SportsEsports style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <SportsEsports className="custom-class" />;

export const Clickable = () => <SportsEsports onClick={()=>console.log("clicked")} />;

const Template = (args) => <SportsEsports {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
