import React from "react";
import { CarRental } from "./CarRental";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "CarRental",
  component: CarRental,
};

export const Default = () => <CarRental />;
export const Fill = () => <CarRental fill="blue" />;
export const Size = () => <CarRental height="50" width="50" />;
export const CustomStyle = () => <CarRental style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <CarRental className="custom-class" />;

export const Clickable = () => <CarRental onClick={()=>console.log("clicked")} />;

const Template = (args) => <CarRental {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
